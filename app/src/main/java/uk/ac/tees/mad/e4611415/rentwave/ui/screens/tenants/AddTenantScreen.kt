package uk.ac.tees.mad.e4611415.rentwave.ui.screens.tenants

// 📌 Required Imports
import android.app.DatePickerDialog
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(navController: NavHostController) {

    val context = LocalContext.current
    val functions = Firebase.functions
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var rentAmount by remember { mutableStateOf("") }

    var selectedPropertyId by remember { mutableStateOf("") }
    var properties by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var expanded by remember { mutableStateOf(false) }

    var rentStartDate by remember { mutableStateOf("") }
    var rentEndDate by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            rentStartDate = dateFormatter.format(calendar.time)

            calendar.add(Calendar.MONTH, 1)
            rentEndDate = dateFormatter.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // 🔹 Load landlord properties
    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    properties = snapshot.documents.map {
                        it.data!! + mapOf("id" to it.id)
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Tenant", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
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

            OutlinedTextField(firstName, { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(lastName, { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Tenant Email") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(
                phone,
                { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                rentAmount,
                { rentAmount = it },
                label = { Text("Monthly Rent (£)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // 🔹 Property dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = properties.find { it["id"] == selectedPropertyId }?.get("name")?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("Select Property") },
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

            Spacer(Modifier.height(15.dp))

            // 🔹 Date Picker box—fully clickable UI
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = rentStartDate,
                    onValueChange = {},
                    label = { Text("Rent Start Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { datePicker.show() }
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {

                    if (firstName.isBlank() || lastName.isBlank() ||
                        phone.isBlank() || email.isBlank() ||
                        rentAmount.isBlank() || selectedPropertyId.isBlank() ||
                        rentStartDate.isBlank()
                    ) {
                        errorMessage = "All fields are required!"
                        return@Button
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Invalid email!"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = ""

                    val tempPassword = (100000..999999).random().toString()
                    val selectedPropertyName =
                        properties.find { it["id"] == selectedPropertyId }?.get("name")?.toString() ?: ""

                    val data = hashMapOf(
                        "email" to email,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "phone" to phone,
                        "tempPassword" to tempPassword,
                        "propertyId" to selectedPropertyId,
                        "propertyName" to selectedPropertyName,
                        "landlordId" to userId,
                        "rentAmount" to rentAmount,
                        "rentStartDate" to rentStartDate,
                        "nextRentDate" to rentEndDate
                    )

                    functions.getHttpsCallable("createTenant")
                        .call(data)
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "Tenant created & Email sent ✔", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener { err ->
                            isLoading = false

                            val message = when {
                                err.message?.contains("auth/email-already-exists", true) == true ->
                                    "This email is already registered. Try another email."
                                else -> err.message ?: "Failed to create tenant"
                            }

                            errorMessage = message
                        }


                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else Text("Add Tenant")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
