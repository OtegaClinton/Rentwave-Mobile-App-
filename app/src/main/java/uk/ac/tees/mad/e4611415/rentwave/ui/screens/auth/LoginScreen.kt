package uk.ac.tees.mad.e4611415.rentwave.ui.screens.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.R
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {

    // Input state for login form
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Password visibility state
    var passwordVisible by remember { mutableStateOf(false) }

    // Error message visibility + resend email option
    var errorMessage by remember { mutableStateOf("") }
    var showResend by remember { mutableStateOf(false) }

    // Loading state for login button (Updated for Spinner)
    var isLoading by remember { mutableStateOf(false) }

    // Firebase authentication references
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // App Logo
            Image(
                painter = painterResource(id = R.drawable.rentwave_logo),
                contentDescription = null,
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Welcome title
            Text(
                "Welcome Back",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Login to your Rentwave account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email text field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password text field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Forgot Password navigation
            Text(
                "Forgot Password?",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Login button triggers Firebase authentication (Fixed role check)
            Button(
                onClick = {

                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill email and password"
                        showResend = false
                        return@Button
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Invalid email format"
                        showResend = false
                        return@Button
                    }

                    isLoading = true // Start spinner

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false // Stop spinner
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null && user.isEmailVerified) {

                                    val userId = user.uid

                                    // First check "users" collection (Landlords + Tenants)
                                    db.collection("users").document(userId).get()
                                        .addOnSuccessListener { doc ->
                                            val role = doc.getString("role")
                                            when (role) {
                                                "landlord" -> {
                                                    navController.navigate(Screen.LandlordDashboard.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                                "tenant" -> {
                                                    navController.navigate(Screen.TenantDashboard.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                                else -> {
                                                    // Fallback check tenants collection
                                                    db.collection("tenants").document(userId).get()
                                                        .addOnSuccessListener { tenantDoc ->
                                                            if (tenantDoc.exists()) {
                                                                navController.navigate(Screen.TenantDashboard.route) {
                                                                    popUpTo(Screen.Login.route) { inclusive = true }
                                                                }
                                                            } else {
                                                                // ✅ User authenticated but not registered in app
                                                                FirebaseAuth.getInstance().signOut()
                                                                errorMessage = "No account found. Please sign up to continue."
                                                                showResend = false
                                                            }
                                                        }
                                                }

                                            }
                                        }

                                } else {
                                    errorMessage = "Please verify your email first"
                                    showResend = true
                                }
                            } else {
                                errorMessage = "Invalid login credentials"
                                showResend = false
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !isLoading // Disable while loading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login", fontWeight = FontWeight.Bold)
                }
            }

            // Display login errors
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            // Offer to resend verification email
            if (showResend) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Resend Verification Email",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        auth.currentUser?.sendEmailVerification()
                        Toast.makeText(context, "Verification email resent!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation to sign up screen
            Row {
                Text("Don't have an account? ")
                Text(
                    "Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }
        }
    }
}
