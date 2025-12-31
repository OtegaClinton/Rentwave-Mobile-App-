package uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantPaymentsHomeScreen(navController: NavHostController) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var rentAmount by remember { mutableStateOf("") }
    var nextDueFormatted by remember { mutableStateOf("Loading…") }
    var statusText by remember { mutableStateOf("Loading…") }
    var statusColor by remember { mutableStateOf(Color.Gray) }

    // ✅ Matches Firestore: "31 Jan 2026"
    val firestoreDateParser = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    }

    val displayFormat = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    /* ---------------- FETCH RENT INFO ---------------- */

    LaunchedEffect(userId) {
        db.collection("tenants").document(userId)
            .get()
            .addOnSuccessListener { doc ->

                rentAmount = doc.getString("rentAmount") ?: "0"
                val nextDueRaw = doc.getString("nextRentDate")

                if (nextDueRaw.isNullOrBlank()) {
                    nextDueFormatted = "Unknown"
                    statusText = "Status Unknown"
                    statusColor = Color.Gray
                    return@addOnSuccessListener
                }

                val parsedDate = try {
                    firestoreDateParser.parse(nextDueRaw)
                } catch (_: Exception) {
                    null
                }

                if (parsedDate == null) {
                    nextDueFormatted = "Unknown"
                    statusText = "Status Unknown"
                    statusColor = Color.Gray
                } else {
                    nextDueFormatted = displayFormat.format(parsedDate)

                    val diffDays =
                        ((parsedDate.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()

                    when {
                        diffDays < 0 -> {
                            statusText = "Overdue by ${-diffDays} days"
                            statusColor = Color.Red
                        }
                        diffDays in 0..7 -> {
                            statusText = "Due Soon ($diffDays days left)"
                            statusColor = Color(0xFFFF9800)
                        }
                        else -> {
                            statusText = "Up to Date"
                            statusColor = Color(0xFF4CAF50)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load payment info", Toast.LENGTH_SHORT).show()
                nextDueFormatted = "Unknown"
                statusText = "Status Unknown"
                statusColor = Color.Gray
            }
    }

    /* ---------------- UI ---------------- */

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payments", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            /* 💳 PAYMENT SUMMARY */

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(18.dp)) {

                    Text(
                        "Rent Amount: £$rentAmount",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(4.dp))
                    Text("Next Rent Due: $nextDueFormatted")

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        color = statusColor,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = statusText,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            /* 🔵 PAY RENT */

            Button(
                onClick = { navController.navigate(Screen.TenantPayment.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pay Rent")
            }

            /* 📄 PAYMENT HISTORY */

            OutlinedButton(
                onClick = { navController.navigate(Screen.PaymentHistory.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Payment History")
            }
        }
    }
}
