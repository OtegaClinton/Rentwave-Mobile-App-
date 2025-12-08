package uk.ac.tees.mad.e4611415.rentwave.ui.screens.tenants

// Imports
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(navController: NavHostController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val functions = Firebase.functions
    val auth = FirebaseAuth.getInstance()

    // Form states
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedPropertyId by remember { mutableStateOf("") }
    var properties by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    // UI states
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val userId = auth.currentUser?.uid

    // Fetch landlord properties
    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    properties = snapshot.documents.map { it.data!! + mapOf("id" to it.id) }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Failed to load properties!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add New Tenant", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Text fields ---
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Tenant Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // --- Property dropdown ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = properties
                        .find { it["id"] == selectedPropertyId }
                        ?.get("name")
                        ?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("Select Property") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    properties.forEach { property ->
                        DropdownMenuItem(
                            text = { Text(property["name"].toString()) },
                            onClick = {
                                selectedPropertyId = property["id"].toString()
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Add Tenant Button ---
            Button(
                onClick = {
                    // Basic validation
                    if (
                        firstName.isBlank() ||
                        lastName.isBlank() ||
                        email.isBlank() ||
                        phone.isBlank() ||
                        selectedPropertyId.isBlank()
                    ) {
                        errorMessage = "All fields are required!"
                        return@Button
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Invalid email format!"
                        return@Button
                    }

                    val landlordId = userId
                    if (landlordId == null) {
                        errorMessage = "Landlord not logged in."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = ""

                    val tempPassword = firstName.lowercase() + (1000..9999).random()

                    val tenantData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "phone" to phone,
                        "propertyId" to selectedPropertyId,
                        "role" to "tenant",
                        "landlordId" to landlordId
                    )

                    // 1️⃣ Save tenant to Firestore first
                    db.collection("tenants").add(tenantData)
                        .addOnSuccessListener {

                            // 2️⃣ Call Cloud Function to create Auth + send email
                            functions
                                .getHttpsCallable("createTenant")
                                .call(
                                    mapOf(
                                        "email" to email,
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "phone" to phone,
                                        "tempPassword" to tempPassword,
                                        "propertyId" to selectedPropertyId,
                                        "landlordId" to landlordId
                                    )
                                )
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Tenant added successfully & email sent!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener { err ->
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Tenant saved but email failed: ${err.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = "Failed to save tenant"
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Add Tenant")
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
