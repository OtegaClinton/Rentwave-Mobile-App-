package uk.ac.tees.mad.e4611415.rentwave.ui.screens.properties

// Required imports
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(navController: NavHostController) {

    // Current logged-in landlord
    val user = FirebaseAuth.getInstance().currentUser

    // Firebase access
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Store list of properties from Firestore
    var properties by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }

    // Loading indicator state
    var isLoading by remember { mutableStateOf(true) }

    // Fetch landlord properties in realtime using snapshot listener
    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, "Failed to fetch properties", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        properties = snapshot.documents.map { doc -> doc.id to (doc.data ?: emptyMap()) }
                        isLoading = false
                    }
                }
        }
    }

    // Screen Layout
    Scaffold(

        // Top AppBar
        topBar = {
            TopAppBar(
                title = { Text("Your Properties", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        // FAB → Add new property
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProperty.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Property")
            }
        }
    ) { pad ->

        Box(modifier = Modifier.fillMaxSize().padding(pad)) {

            // Show loading spinner on first load
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Empty state message
            else if (properties.isEmpty()) {
                Text(
                    text = "You haven't added any properties yet.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Show property list
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(properties) { (propertyId, property) ->

                        // First image to use as cover (can be null)
                        val images = property["images"] as? List<*> ?: emptyList<Any>()
                        val coverImage = images.firstOrNull()?.toString()

                        // Property Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Navigate to Property Details Screen
                                    navController.navigate(Screen.PropertyDetails.passId(propertyId))
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {

                            // Image at the top (full width)
                            if (coverImage != null) {
                                AsyncImage(
                                    model = coverImage,
                                    contentDescription = "Property Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                )
                            }

                            // Property text content
                            Column(modifier = Modifier.padding(12.dp)) {

                                // Property Name
                                Text(
                                    text = property["name"]?.toString() ?: "Unnamed Property",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // Price
                                Text(
                                    text = "£${property["price"] ?: "N/A"} / month",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Location
                                Text(
                                    text = property["location"]?.toString() ?: "No location",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
