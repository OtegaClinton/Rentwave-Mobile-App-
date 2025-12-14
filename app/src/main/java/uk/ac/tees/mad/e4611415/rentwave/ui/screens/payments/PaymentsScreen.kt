package uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(navController: NavHostController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val landlordId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var payments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var tenantNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("payments")
            .whereEqualTo("landlordId", landlordId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Failed to load payments", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val docs = snapshot.documents

                    payments = docs.mapNotNull { doc ->
                        doc.data?.plus("id" to doc.id)
                    }.sortedByDescending {
                        (it["timestamp"] as? Timestamp)?.toDate()
                    }

                    // 🔹 Fetch tenant names
                    val tenantIds = payments.mapNotNull { it["userId"]?.toString() }.distinct()

                    tenantIds.forEach { tenantId ->
                        db.collection("users").document(tenantId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val name =
                                    "${userDoc.getString("firstName") ?: ""} ${userDoc.getString("lastName") ?: ""}".trim()
                                tenantNames = tenantNames + (tenantId to name)
                            }
                    }

                    isLoading = false
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payments", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
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
                .padding(16.dp)
        ) {

            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                payments.isEmpty() -> Text(
                    "No payments yet",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(payments) { payment ->
                        PaymentCard(
                            payment = payment,
                            tenantName = tenantNames[payment["userId"]?.toString()],
                            onClick = {
                                navController.navigate(
                                    Screen.LandlordPaymentDetails.passId(
                                        payment["id"].toString()
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
fun PaymentCard(
    payment: Map<String, Any>,
    tenantName: String?,
    onClick: () -> Unit
) {

    val status = payment["status"]?.toString() ?: "pending"
    val isPending = status == "pending"

    val statusColor =
        if (isPending) Color(0xFFFF9800) else Color(0xFF2E7D32)

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = (payment["timestamp"] as? Timestamp)
        ?.toDate()
        ?.let { dateFormatter.format(it) } ?: "Unknown date"

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
                text = tenantName ?: "Unknown Tenant",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text("Amount: £${payment["amount"] ?: 0}")
            Text("Paid on: $formattedDate")

            Spacer(Modifier.height(6.dp))

            Text(
                "Status: ${status.replaceFirstChar { it.uppercase() }}",
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
