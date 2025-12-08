package uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var payments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch all payments from landlord's properties
    LaunchedEffect(userId) {
        userId?.let { uid ->
            // Step 1: Fetch all landlord properties
            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { propertiesSnapshot ->
                    val propertyIds = propertiesSnapshot.documents.map { it.id }

                    if (propertyIds.isEmpty()) {
                        payments = emptyList()
                        isLoading = false
                        return@addOnSuccessListener
                    }

                    // Step 2: Fetch payments for those property IDs
                    db.collection("payments")
                        .whereIn("propertyId", propertyIds)
                        .get()
                        .addOnSuccessListener { paymentsSnapshot ->
                            payments = paymentsSnapshot.documents.mapNotNull { it.data }
                            isLoading = false
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to load payments", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load properties", Toast.LENGTH_SHORT).show()
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
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                payments.isEmpty() -> {
                    Text(
                        text = "You have no payments yet.",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(payments) { payment ->
                            PaymentCard(payment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentCard(payment: Map<String, Any>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tenant: ${payment["tenantName"] ?: "N/A"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text("Property: ${payment["propertyName"] ?: "Unknown"}")
            Text("Amount: £${payment["amount"] ?: 0}")
            Text("Date: ${payment["date"] ?: "N/A"}")

            val status = payment["status"]?.toString() ?: "Pending"
            val statusColor = when (status.lowercase()) {
                "paid" -> Color(0xFF2E7D32)
                "failed" -> Color(0xFFC62828)
                else -> Color(0xFFFFA000)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Status: $status",
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}
