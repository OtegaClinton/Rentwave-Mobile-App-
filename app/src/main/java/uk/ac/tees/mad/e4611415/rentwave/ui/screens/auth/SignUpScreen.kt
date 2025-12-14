package uk.ac.tees.mad.e4611415.rentwave.ui.screens.auth

import android.app.DatePickerDialog
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4611415.rentwave.R
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavHostController) {

    // Form states
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Password visibility states
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Messages + loading state
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo
            Image(
                painter = painterResource(id = R.drawable.rentwave_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Register as a landlord on Rentwave",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // All your fields remain unchanged
            OutlinedTextField(firstName, { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(lastName, { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = sex,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sex") },
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Male", "Female", "Others").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                sex = option
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                context,
                { _, year, month, day -> dob = "$day/${month+1}/$year" },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )
            OutlinedTextField(dob, { dob = it }, label = { Text("Date of Birth(dd/mm/yyyy)") }, modifier = Modifier.fillMaxWidth().clickable { datePicker.show() })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(address, { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(password, { password = it }, label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "")
                }}
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(confirmPassword, { confirmPassword = it }, label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "")
                }}
            )
            Spacer(Modifier.height(16.dp))

            // Signup Button Logic (unchanged UI)
            Button(
                onClick = {
                    val normalizedEmail = email.trim().lowercase()

                    if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                        errorMessage = "Enter a valid email"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match!"
                        return@Button
                    }

                    errorMessage = ""
                    isLoading = true

                    auth.createUserWithEmailAndPassword(normalizedEmail, password)
                        .addOnSuccessListener {
                            val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                            val userData = mapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "email" to normalizedEmail,
                                "phone" to phone,
                                "sex" to sex,
                                "dob" to dob,
                                "address" to address,
                                "role" to "landlord",
                                "createdAt" to FieldValue.serverTimestamp()
                            )

                            db.collection("users").document(uid).set(userData)
                                .addOnSuccessListener {
                                    isLoading = false
                                    successMessage = "Account created! Verify your email before login."

                                    auth.currentUser?.sendEmailVerification()

                                    coroutineScope.launch {
                                        delay(3000)
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.SignUp.route) { inclusive = true }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    errorMessage = "Failed to save user data"
                                }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = it.localizedMessage ?: "Signup failed"
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading)
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                else
                    Text("Sign Up")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            if (successMessage.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(successMessage, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(16.dp))

            // Your navigation and TERMS section remain exactly the same 👍
            Row {
                Text("Already have an account? ")
                Text("Login", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate(Screen.Login.route) })
            }

            Spacer(Modifier.height(8.dp))
            Text("By creating an account, you agree to Rentwave’s", textAlign = TextAlign.Center)
            Text("Terms and Conditions",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate(Screen.Terms.route) },
                textAlign = TextAlign.Center
            )
        }
    }
}
