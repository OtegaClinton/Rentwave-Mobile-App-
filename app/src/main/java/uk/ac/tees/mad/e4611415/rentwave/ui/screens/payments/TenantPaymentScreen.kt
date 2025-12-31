package uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantPaymentScreen(navController: NavHostController) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var rentAmount by remember { mutableStateOf("") }
    var landlordId by remember { mutableStateOf("") }
    var nextRentDate by remember { mutableStateOf<Date?>(null) }

    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    /* 🔹 Date formatter used everywhere */
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    }

    /* 🔹 Fetch Tenant Rent Data */
    LaunchedEffect(userId) {
        db.collection("tenants").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                rentAmount = doc.getString("rentAmount") ?: "0"
                landlordId = doc.getString("landlordId") ?: ""

                val nextRentDateString = doc.getString("nextRentDate")
                nextRentDate = try {
                    nextRentDateString?.let { dateFormatter.parse(it) }
                } catch (_: Exception) {
                    null
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Failed to load rent details",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay Rent", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
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

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Text(
                text = "Total Rent: £$rentAmount",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 16) cardNumber = it },
                label = { Text("Card Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = expiryDate,
                onValueChange = { if (it.length <= 5) expiryDate = it },
                label = { Text("MM/YY") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cvv,
                onValueChange = { if (it.length <= 3) cvv = it },
                label = { Text("CVV") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {

                    if (cardNumber.length < 16 || expiryDate.length < 4 || cvv.length < 3) {
                        Toast.makeText(
                            context,
                            "Invalid card details!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    isProcessing = true

                    val paymentData = mapOf(
                        "userId" to userId,
                        "landlordId" to landlordId,
                        "amount" to rentAmount,
                        "timestamp" to Date(),
                        "status" to "Paid"
                    )

                    /* 🔹 Save Payment */
                    db.collection("payments")
                        .add(paymentData)
                        .addOnSuccessListener { docRef ->

                            val paymentId = docRef.id

                            /* 🔹 Move next rent date forward */
                            val cal = Calendar.getInstance()
                            cal.time = nextRentDate ?: Date()
                            cal.add(Calendar.MONTH, 1)

                            db.collection("tenants").document(userId)
                                .update("nextRentDate", dateFormatter.format(cal.time))

                            /* 🔹 Trigger receipt email */
                            com.google.firebase.functions.FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("sendPaymentReceipt")
                                .call(mapOf("paymentId" to paymentId))
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Receipt Sent 📩",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            isProcessing = false

                            Toast.makeText(
                                context,
                                "Payment Successful 🎉",
                                Toast.LENGTH_LONG
                            ).show()

                            navController.navigate(Screen.MyRent.route) {
                                popUpTo(Screen.TenantPayment.route) { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            isProcessing = false
                            Toast.makeText(
                                context,
                                "Payment Failed!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Confirm Payment")
                }
            }
        }
    }
}
