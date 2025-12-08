package uk.ac.tees.mad.e4611415.rentwave.ui.screens.properties

// Required imports
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
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
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsScreen(navController: NavHostController, propertyId: String) {

    // Get context and Firestore reference
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Hold property details from Firestore
    var property by remember { mutableStateOf<Map<String, Any>?>(null) }

    // Loading state for first fetch
    var isLoading by remember { mutableStateOf(true) }

    // Delete dialog + loading state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteLoading by remember { mutableStateOf(false) }

    // Fetch property details once
    LaunchedEffect(propertyId) {
        db.collection("properties").document(propertyId)
            .get()
            .addOnSuccessListener { doc ->
                property = doc.data
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load property details", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    // Top-level screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Property Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        // Show loading spinner first time
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // If property was loaded successfully
        property?.let { details ->

            // Get images list (may be empty)
            val images = details["images"] as? List<*> ?: emptyList<String>()

            // State to track current visible image index (for dots)
            val listState = rememberLazyListState()
            val currentIndex by remember {
                derivedStateOf { listState.firstVisibleItemIndex.coerceAtLeast(0) }
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ----------------------------
                // IMAGE CAROUSEL (LazyRow)
                // ----------------------------
                if (images.isNotEmpty()) {
                    LazyRow(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        itemsIndexed(images) { _, img ->
                            AsyncImage(
                                model = img,
                                contentDescription = "Property Image",
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .fillMaxHeight()
                            )
                        }
                    }

                    // Dots indicator below images
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(images.size) { index ->
                            val color =
                                if (index == currentIndex) MaterialTheme.colorScheme.primary
                                else Color.LightGray

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(3.dp)
                                    .background(color = color, shape = CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ----------------------------
                // PROPERTY MAIN TEXT DETAILS
                // ----------------------------

                // Name
                Text(
                    text = details["name"]?.toString() ?: "Unnamed Property",
                    style = MaterialTheme.typography.titleLarge
                )

                // Price
                Text(
                    text = "£${details["price"] ?: "N/A"} / month",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Location
                Text(
                    text = "Location: ${details["location"] ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = details["description"]?.toString() ?: "No description added.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ----------------------------
                // ACTION BUTTONS
                // ----------------------------

                // Edit property button
                Button(
                    onClick = {
                        navController.navigate(Screen.EditProperty.passId(propertyId))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Edit Property")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Delete property button
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete Property")
                }
            }
        }
    }

    // ----------------------------
    // DELETE CONFIRMATION DIALOG
    // ----------------------------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Property") },
            text = { Text("Are you sure you want to delete this property?") },
            confirmButton = {
                if (deleteLoading) {
                    CircularProgressIndicator()
                } else {
                    TextButton(
                        onClick = {
                            deleteLoading = true
                            // Delete the document from Firestore
                            db.collection("properties").document(propertyId)
                                .delete()
                                .addOnSuccessListener {
                                    deleteLoading = false
                                    Toast.makeText(context, "Property deleted ✔", Toast.LENGTH_SHORT).show()
                                    showDeleteDialog = false

                                    // Go back to properties list
                                    navController.navigate(Screen.Properties.route) {
                                        popUpTo(Screen.PropertyDetails.route) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    deleteLoading = false
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                }
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
