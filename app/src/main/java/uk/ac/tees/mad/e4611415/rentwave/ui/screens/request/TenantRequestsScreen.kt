package uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
fun TenantRequestsScreen(navController: NavHostController) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("tenant_requests")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Failed to load requests", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    requests = snapshot.documents.mapNotNull { doc ->
                        doc.data?.toMutableMap()?.apply { put("id", doc.id) }
                    }.sortedByDescending {
                        (it["timestamp"] as? Timestamp)?.toDate()
                    }
                    isLoading = false
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Requests", color = Color.White) },
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
        },

        /* ✅ ADD REQUEST BUTTON (RESTORED) */
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.TenantCreateRequest.route)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Request")
            }
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
                    "You haven't submitted any requests yet.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests) { request ->
                        TenantRequestCard(
                            request = request,
                            onClick = {
                                navController.navigate(
                                    Screen.TenantRequestDetails.passId(
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
fun TenantRequestCard(
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
                text = request["title"]?.toString() ?: "No Title",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Status: $status",
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
