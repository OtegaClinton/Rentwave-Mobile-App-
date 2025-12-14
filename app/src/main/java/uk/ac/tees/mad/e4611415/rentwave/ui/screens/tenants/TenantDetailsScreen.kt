package uk.ac.tees.mad.e4611415.rentwave.ui.screens.tenants

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailsScreen(navController: NavHostController, tenantId: String) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var tenant by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteLoading by remember { mutableStateOf(false) }

    LaunchedEffect(tenantId) {
        db.collection("tenants").document(tenantId)
            .get()
            .addOnSuccessListener { doc ->
                tenant = doc.data
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load tenant details", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Details", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        tenant?.let { details ->

            val firstName = details["firstName"]?.toString() ?: "N/A"
            val lastName = details["lastName"]?.toString() ?: ""
            val email = details["email"]?.toString() ?: "N/A"
            val phone = details["phone"]?.toString() ?: "N/A"
            val propertyName = details["propertyName"]?.toString() ?: "Not assigned yet"

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text("$firstName $lastName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text("📩 Email: $email")
                Text("📞 Phone: $phone")
                Text("🏠 Property: $propertyName")

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Remove Tenant")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Tenant") },
            text = { Text("Are you sure you want to remove this tenant?") },
            confirmButton = {
                if (deleteLoading) {
                    CircularProgressIndicator()
                } else {
                    TextButton(onClick = {
                        deleteLoading = true
                        FirebaseFirestore.getInstance().collection("tenants").document(tenantId)
                            .delete()
                            .addOnSuccessListener {
                                deleteLoading = false
                                Toast.makeText(context, "Tenant Removed", Toast.LENGTH_SHORT).show()
                                navController.navigate(Screen.Tenants.route) {
                                    popUpTo(Screen.TenantDetails.route) { inclusive = true }
                                }
                            }
                            .addOnFailureListener {
                                deleteLoading = false
                                Toast.makeText(context, "Failed to remove tenant", Toast.LENGTH_SHORT).show()
                            }
                    }) {
                        Text("Remove", color = Color.Red)
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
