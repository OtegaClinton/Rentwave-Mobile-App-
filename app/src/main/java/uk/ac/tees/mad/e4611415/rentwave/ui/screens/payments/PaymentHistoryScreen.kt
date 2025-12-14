package uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(navController: NavHostController) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var payments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val dateFormatter = SimpleDateFormat("dd MMM yyyy 'at' hh:mm a", Locale.getDefault())
    val fallbackParser = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH)

    // Fetch payment history
    LaunchedEffect(Unit) {
        db.collection("payments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->

                val list = snapshot.documents.mapNotNull { it.data }

                // ---- SORT BY MOST RECENT ----
                payments = list.sortedByDescending { payment ->
                    when (val ts = payment["timestamp"]) {
                        is Timestamp -> ts.toDate().time
                        is Date -> ts.time
                        is String -> try { fallbackParser.parse(ts)?.time ?: 0L } catch (_: Exception) { 0L }
                        else -> 0L
                    }
                }

                loading = false
            }
            .addOnFailureListener {
                loading = false
                Toast.makeText(context, "Failed to load payment history.", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                payments.isEmpty() -> Text(
                    "No payment history found.",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(payments) { payment ->

                        val amount = payment["amount"].toString()
                        val status = payment["status"].toString()

                        // ---- FORMAT DATE SAFELY ----
                        val formattedDate = when (val ts = payment["timestamp"]) {
                            is Timestamp -> dateFormatter.format(ts.toDate())
                            is Date -> dateFormatter.format(ts)
                            is String -> {
                                try { dateFormatter.format(fallbackParser.parse(ts)!!) }
                                catch (e: Exception) { "Unknown date" }
                            }
                            else -> "Unknown date"
                        }

                        val receiptUrl = payment["receiptUrl"]?.toString()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {

                            Column(Modifier.padding(16.dp)) {

                                Text("Amount: £$amount", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(6.dp))

                                Text("Date: $formattedDate")
                                Text("Status: $status")

                                Spacer(Modifier.height(10.dp))

                                if (!receiptUrl.isNullOrBlank()) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(receiptUrl))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Download Receipt")
                                    }
                                } else {
                                    Text("Receipt not available", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
