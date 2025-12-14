package uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandlordPaymentDetailsScreen(
    navController: NavHostController,
    paymentId: String
) {

    val db = FirebaseFirestore.getInstance()
    var payment by remember { mutableStateOf<Map<String, Any>?>(null) }
    var tenant by remember { mutableStateOf<Map<String, Any>?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(paymentId) {
        db.collection("payments").document(paymentId)
            .get()
            .addOnSuccessListener { doc ->
                payment = doc.data

                val tenantId = doc.getString("userId")
                tenantId?.let {
                    db.collection("users").document(it).get()
                        .addOnSuccessListener { tenantDoc ->
                            tenant = tenantDoc.data
                            loading = false
                        }
                } ?: run { loading = false }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Details", color = Color.White) },
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

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            payment == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Payment not found")
            }

            else -> {

                val formatter =
                    SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())

                val date = (payment!!["timestamp"] as? Timestamp)?.toDate()
                val formattedDate = date?.let { formatter.format(it) } ?: "Unknown"

                val status = payment!!["status"].toString()
                val statusColor =
                    if (status.equals("paid", true)) Color(0xFF2E7D32)
                    else Color(0xFFFF9800)

                Card(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        /* 🔹 HEADER */
                        Text(
                            "£${payment!!["amount"]}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            status.replaceFirstChar { it.uppercase() },
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            "Paid on $formattedDate",
                            color = Color.Gray
                        )

                        Divider()

                        /* 🔹 PROPERTY */
                        SectionTitle("Property")

                        val propertyName =
                            payment!!["propertyName"]?.toString()
                                ?: tenant?.get("propertyName")?.toString()
                                ?: "Unknown property"

                        Text(propertyName)

                        Divider()

                        /* 🔹 TENANT */
                        SectionTitle("Tenant")
                        Text(
                            "${tenant?.get("firstName")} ${tenant?.get("lastName")}",
                            fontWeight = FontWeight.Medium
                        )
                        Text(tenant?.get("email").toString(), color = Color.Gray)

                        Divider()

                        /* 🔹 META (SUBTLE) */
                        SectionTitle("Reference")
                        MetaRow("Tenant ID", payment!!["userId"].toString())
                        MetaRow("Landlord ID", payment!!["landlordId"].toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
private fun MetaRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
