package uk.ac.tees.mad.e4611415.rentwave.ui.screens.rent

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRentScreen(navController: NavHostController) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var tenantData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var landlordData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val fallbackDateFormatter =
        SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH)
    val displayFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LaunchedEffect(userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                tenantData = doc.data
                val landlordId = doc.getString("landlordId")

                if (!landlordId.isNullOrEmpty()) {
                    db.collection("users").document(landlordId)
                        .get()
                        .addOnSuccessListener { landlordDoc ->
                            landlordData = landlordDoc.data
                            isLoading = false
                        }
                } else isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load rent details", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Rent", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        Box(
            Modifier.padding(padding).fillMaxSize(),
            Alignment.Center
        ) {

            when {
                isLoading -> CircularProgressIndicator()
                tenantData == null -> Text("No rent details found.")
                else -> {
                    val property = tenantData!!["propertyName"].toString()
                    val rentAmount = tenantData!!["rentAmount"].toString()
                    val rentStart = tenantData!!["rentStartDate"].toString()
                    val nextDueString = tenantData!!["nextRentDate"].toString()

                    val nextDueDate = try {
                        fallbackDateFormatter.parse(nextDueString)
                    } catch (_: Exception) {
                        null
                    }

                    val today = Date()
                    val diffDays = nextDueDate?.let {
                        ((it.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                    }

                    val (status, color) = when {
                        diffDays == null -> "Status Unknown" to Color.Gray
                        diffDays < 0 -> "Overdue by ${abs(diffDays)} days" to Color.Red
                        diffDays in 0..7 -> "Due Soon (in $diffDays days)" to Color(0xFFFF9800)
                        else -> "Up to Date" to Color(0xFF4CAF50)
                    }

                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        StatusBadge(status, color)

                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                LabelValue("Property", property)
                                LabelValue("Monthly Rent", "£$rentAmount")
                                LabelValue("Rent Started", rentStart)
                                LabelValue(
                                    "Next Due",
                                    nextDueDate?.let(displayFormatter::format) ?: "Unknown"
                                )
                            }
                        }

                        landlordData?.let { owner ->
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(Color.White)
                            ) {
                                Column(Modifier.padding(18.dp)) {
                                    Text("Your Landlord", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(6.dp))
                                    LabelValue("Name", "${owner["firstName"]} ${owner["lastName"]}")
                                    LabelValue("Email", owner["email"].toString())
                                }
                            }
                        }

                        Button(
                            onClick = { navController.navigate(Screen.TenantPaymentsHome.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go To Payments")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.padding(10.dp), Alignment.Center) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LabelValue(label: String, value: String) {
    Column {
        Text(label, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
    }
}
