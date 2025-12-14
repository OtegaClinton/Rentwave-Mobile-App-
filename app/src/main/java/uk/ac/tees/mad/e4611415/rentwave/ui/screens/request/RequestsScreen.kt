package uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(navController: NavHostController) {

    val db = FirebaseFirestore.getInstance()
    val landlordId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val context = LocalContext.current

    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("tenant_requests")
            .get()
            .addOnSuccessListener { snapshot ->

                val tempList = mutableListOf<Map<String, Any>>()

                snapshot.documents.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    val tenantId = data["userId"]?.toString() ?: return@forEach

                    db.collection("tenants").document(tenantId)
                        .get()
                        .addOnSuccessListener { tenantDoc ->

                            if (tenantDoc.getString("landlordId") == landlordId) {
                                val merged = data.toMutableMap()
                                merged["id"] = doc.id
                                merged["tenantName"] =
                                    "${tenantDoc.getString("firstName")} ${tenantDoc.getString("lastName")}"
                                merged["propertyName"] =
                                    tenantDoc.getString("propertyName") ?: "Unknown"

                                tempList.add(merged)
                                requests = tempList.sortedByDescending {
                                    (it["timestamp"] as? Timestamp)?.toDate()
                                }
                            }
                            isLoading = false
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load requests", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Requests", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                requests.isEmpty() -> Text(
                    "No tenant requests yet.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium
                )

                else -> LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests) { request ->
                        LandlordRequestCard(
                            request = request,
                            onClick = {
                                // ✅ CORRECT SCREEN FOR LANDLORD
                                navController.navigate(
                                    Screen.LandlordRequestDetails.passId(
                                        request["id"].toString()
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LandlordRequestCard(
    request: Map<String, Any>,
    onClick: () -> Unit
) {

    val status = request["status"]?.toString() ?: "Pending"
    val statusColor = when (status.lowercase()) {
        "resolved" -> Color(0xFF2E7D32)
        "in progress" -> Color(0xFFFF8F00)
        else -> Color(0xFFD32F2F)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                request["title"].toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Text("Tenant: ${request["tenantName"]}")
            Text("Property: ${request["propertyName"]}")

            Spacer(Modifier.height(6.dp))

            Text(
                "Status: $status",
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
