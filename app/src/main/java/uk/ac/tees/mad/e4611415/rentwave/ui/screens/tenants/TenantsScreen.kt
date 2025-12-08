package uk.ac.tees.mad.e4611415.rentwave.ui.screens.tenants

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsScreen(navController: NavHostController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Store tenants list including ID + property name
    var tenants by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    // Show spinner while loading
    var isLoading by remember { mutableStateOf(true) }

    /**
     * Load all tenants where propertyId belongs to this landlord.
     * Also fetch property names and attach them to each tenant.
     */
    LaunchedEffect(userId) {
        userId?.let { uid ->

            // Fetch properties owned by landlord
            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { propertySnapshot ->

                    // Build a simple map: propertyId -> propertyName
                    val propertyMap = mutableMapOf<String, String>()
                    for (doc in propertySnapshot.documents) {
                        val id = doc.id
                        val name = doc.getString("name") ?: "Unknown Property"
                        propertyMap[id] = name
                    }

                    val propertyIds = propertyMap.keys.toList()

                    if (propertyIds.isNotEmpty()) {

                        // Fetch tenants assigned to these properties
                        db.collection("tenants")
                            .whereIn("propertyId", propertyIds)
                            .get()
                            .addOnSuccessListener { tenantSnapshot ->

                                tenants = tenantSnapshot.documents.map { doc ->
                                    val data = doc.data ?: emptyMap<String, Any>()
                                    val pId = data["propertyId"]?.toString()

                                    // Merge tenant data + id + propertyName for UI
                                    data + mapOf(
                                        "id" to doc.id,
                                        "propertyName" to (propertyMap[pId] ?: "Unknown")
                                    )
                                }

                                isLoading = false
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to load tenants", Toast.LENGTH_SHORT).show()
                                isLoading = false
                            }

                    } else {
                        // Landlord has no properties yet
                        tenants = emptyList()
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load properties", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    // TopBar + Add Tenant button
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Tenants", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddTenant.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Tenant")
            }
        }
    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            when {
                // Loading spinner
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                // Message when there are zero tenants
                tenants.isEmpty() -> Text(
                    text = "You haven't added any tenants yet.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium
                )

                // Show tenant list
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(tenants) { tenant ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Navigate to Tenant Details screen with tenantId
                                    navController.navigate(
                                        Screen.TenantDetails.passId(tenant["id"].toString())
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                Text(
                                    text = "${tenant["firstName"]} ${tenant["lastName"]}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text("📩 ${tenant["email"]}")
                                Text("📞 ${tenant["phone"]}")
                                Text("🏠 Property: ${tenant["propertyName"]}")
                            }
                        }
                    }
                }
            }
        }
    }
}
