package uk.ac.tees.mad.e4611415.rentwave

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.input.VisualTransformation

import uk.ac.tees.mad.e4611415.rentwave.ui.theme.RentwaveTheme


// --- Define navigation screens for safe navigation ---
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object Terms : Screen("terms_screen")
    object LandlordDashboard : Screen("landlord_dashboard")
    object TenantDashboard : Screen("tenant_dashboard")
    object Properties : Screen("properties_screen")
    object AddProperty : Screen("add_property_screen")
    object Tenants : Screen("tenants_screen")
    object AddTenant : Screen("add_tenant_screen")
    object Payments : Screen("payments_screen")
    object Requests : Screen("requests_screen")
    object Messages : Screen("messages_screen")
    object Settings : Screen("settings_screen")

}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            RentwaveTheme {
                val navController = rememberNavController()

                // --- Navigation host ---
                NavHost(navController = navController, startDestination = Screen.Splash.route) {
                    composable(Screen.Splash.route) { SplashScreen(navController) }
                    composable(Screen.Login.route) { LoginScreen(navController) }
                    composable(Screen.SignUp.route) { SignUpScreen(navController) }
                    composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
                    composable(Screen.Terms.route) { TermsScreen(navController) }
                    composable(Screen.LandlordDashboard.route) { LandlordDashboardScreen(navController) }
                    composable(Screen.TenantDashboard.route) { TenantDashboardScreen(navController) }
                    composable(Screen.Properties.route) { PropertiesScreen(navController) }
                    composable(Screen.AddProperty.route) { AddPropertyScreen(navController) }
                    composable(Screen.Tenants.route) { TenantsScreen(navController) }
                    composable(Screen.AddTenant.route) { AddTenantScreen(navController) }
                    composable(Screen.Payments.route) { PaymentsScreen(navController) }
                    composable(Screen.Requests.route) { RequestsScreen(navController) }
                    composable(Screen.Messages.route) { MessagesScreen(navController) }
                    composable(Screen.Settings.route) { SettingsScreen(navController) }
                }
            }
        }
    }
}

/* ---------------- SPLASH SCREEN ---------------- */
@Composable
fun SplashScreen(navController: androidx.navigation.NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.rentwave_logo),
                contentDescription = "Rentwave Logo",
                modifier = Modifier.size(300.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Transparent renting, effortless management",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }

    // Navigate to Login after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }
}

/* ---------------- LOGIN SCREEN ---------------- */
@Composable
fun LoginScreen(navController: androidx.navigation.NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showResend by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {

            Image(
                painter = painterResource(id = R.drawable.rentwave_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Login to your Rentwave account", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Forgot Password?",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate(Screen.ForgotPassword.route) })

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Input validation
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in both email and password."
                    showResend = false
                    return@Button
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Invalid email format."
                    showResend = false
                    return@Button
                }
                if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters long."
                    showResend = false
                    return@Button
                }

                // Firebase login
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                if (user.isEmailVerified) {
                                    val userId = user.uid
                                    db.collection("users").document(userId).get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                val role = document.getString("role")
                                                // Redirect based on role
                                                if (role == "landlord") {
                                                    navController.navigate(Screen.LandlordDashboard.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                } else if (role == "tenant") {
                                                    navController.navigate(Screen.TenantDashboard.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                } else {
                                                    errorMessage = "User role not found."
                                                }
                                            } else {
                                                errorMessage = "No profile found for this user."
                                            }
                                        }
                                        .addOnFailureListener {
                                            errorMessage = "Error checking user details."
                                        }
                                    showResend = false
                                } else {
                                    errorMessage = "Please verify your email before logging in."
                                    showResend = true
                                }
                            }
                        } else {
                            errorMessage = "Invalid login credentials."
                            showResend = false
                        }
                    }
            }, modifier = Modifier.fillMaxWidth()) { Text("Login") }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            if (showResend) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Resend Verification Email",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                            Toast.makeText(context, "Verification email resent!", Toast.LENGTH_SHORT).show()
                        }?.addOnFailureListener {
                            Toast.makeText(context, "Failed to resend verification email.", Toast.LENGTH_SHORT).show()
                        }
                    })
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text("Don't have an account? ", color = MaterialTheme.colorScheme.onBackground)
                Text("Sign Up", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                    navController.navigate(Screen.SignUp.route)
                })
            }
        }
    }
}

/* ---------------- SIGN-UP SCREEN ---------------- */
@Composable
fun SignUpScreen(navController: androidx.navigation.NavHostController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.rentwave_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Register as a landlord on Rentwave",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Form fields
            OutlinedTextField(firstName, { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(lastName, { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sex, { sex = it }, label = { Text("Sex (Male/Female/Other)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(dob, { dob = it }, label = { Text("Date of Birth (dd/mm/yyyy)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(address, { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(confirmPassword, { confirmPassword = it }, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up button
            Button(onClick = {
                val emailPattern = Patterns.EMAIL_ADDRESS
                val phonePattern = Regex("^\\d{11}$")
                val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*]).{6,}\$")

                // Individual error messages
                when {
                    firstName.length < 3 -> { errorMessage = "First Name must be at least 3 characters."; return@Button }
                    lastName.length < 3 -> { errorMessage = "Last Name must be at least 3 characters."; return@Button }
                    !emailPattern.matcher(email).matches() -> { errorMessage = "Enter a valid email address."; return@Button }
                    !phonePattern.matches(phone) -> { errorMessage = "Phone number must be exactly 11 digits."; return@Button }
                    !passwordPattern.matches(password) -> { errorMessage = "Password must be at least 6 characters, include 1 uppercase & 1 special character."; return@Button }
                    password != confirmPassword -> { errorMessage = "Passwords do not match."; return@Button }
                }

                // Firebase sign-up
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                            auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                                Toast.makeText(context, "Verification email sent! Check your inbox.", Toast.LENGTH_SHORT).show()
                            }

                            val user = hashMapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "email" to email,
                                "phone" to phone,
                                "sex" to sex,
                                "dob" to dob,
                                "address" to address,
                                "role" to "landlord"
                            )
                            db.collection("users").document(uid).set(user)
                                .addOnSuccessListener {
                                    successMessage = "Sign-up successful! Please check your email to verify your account."

                                    // Auto-redirect after 5 seconds
                                    coroutineScope.launch {
                                        delay(5000)
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.SignUp.route) { inclusive = true }
                                        }
                                    }

                                }.addOnFailureListener { e -> errorMessage = e.message ?: "Failed to save user data." }

                        } else errorMessage = task.exception?.message ?: "Sign-up failed."
                    }

            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Show error or success messages
            if (errorMessage.isNotEmpty()) Text(errorMessage, color = MaterialTheme.colorScheme.error)
            if (successMessage.isNotEmpty()) Text(successMessage, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text("Already have an account? ", color = MaterialTheme.colorScheme.onBackground)
                Text("Login", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { navController.navigate(Screen.Login.route) })
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("By creating an account, you agree to Rentwave’s", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
            Text(
                "Terms and Conditions",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.Terms.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))

       }
    }
}

/* ---------------- TERMS SCREEN ---------------- */
@Composable
fun TermsScreen(navController: androidx.navigation.NavHostController) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text("← Back", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { navController.popBackStack() })
            Spacer(modifier = Modifier.height(16.dp))
            Text("Terms and Conditions", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                """
                Welcome to Rentwave! By using our platform, you agree to the following terms:

                1. Account Use  
                You must provide accurate personal information when creating an account. Only landlords can sign up; tenants are added by landlords.

                2. Property Listings  
                Landlords are responsible for the accuracy and legality of their property listings.

                3. Privacy & Data Protection  
                Rentwave respects your privacy. Your data will only be used to support your rental activities and will not be shared without consent.

                4. Communication  
                By creating an account, you agree to receive important updates about your listings or account.

                5. Prohibited Activities  
                Users must not misuse Rentwave or engage in fraudulent or illegal activities.

                6. Modifications  
                Rentwave reserves the right to update these terms as necessary, with notice to users.

                7. Liability  
                Rentwave is not responsible for landlord-tenant disputes. Use of the platform is at your own risk.

                8. Governing Law  
                These terms are governed by applicable laws in your region.

                © 2025 Rentwave. All Rights Reserved.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/* ---------------- FORGOT PASSWORD ---------------- */
@Composable
fun ForgotPasswordScreen(navController: androidx.navigation.NavHostController) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)) {

            Image(
                painter = painterResource(id = R.drawable.rentwave_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Forgot Password", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank()) {
                        message = "Please enter your email."
                        return@Button
                    }

                    val normalizedEmail = email.trim().lowercase()

                    // Check Firestore if user exists
                    db.collection("users")
                        .whereEqualTo("email", normalizedEmail)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                message = "Email not registered. Please check and try again."
                            } else {
                                // Email exists in Firestore -> send reset email
                                auth.sendPasswordResetEmail(normalizedEmail)
                                    .addOnSuccessListener {
                                        message =
                                            "Password reset link sent! Please check your email."
                                        Toast.makeText(
                                            context,
                                            "Password reset link sent to $normalizedEmail",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        message =
                                            e.message ?: "Failed to send reset link. Try again."
                                    }
                            }
                        }
                        .addOnFailureListener {
                            message = "Error checking email. Please try again."
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Reset Link")
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (message.isNotEmpty()) {
                Text(message, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "← Back to Login",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate(Screen.Login.route) }
            )
        }
    }
}



/* ---------------- DASHBOARDS ---------------- */
// --------------------- LANDLORD DASHBOARD ---------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandlordDashboardScreen(navController: NavHostController) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }

    // Fetch landlord data
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                firstName = doc.getString("firstName") ?: ""
                profileImageUrl = doc.getString("profileImageUrl") ?: ""
            }
        }
    }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val fileName = "profile_images/${user?.uid}.jpg"
            val ref = storageRef.child(fileName)
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        profileImageUrl = url.toString()
                        db.collection("users").document(user!!.uid)
                            .update("profileImageUrl", profileImageUrl)
                        Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val items = listOf(
        "Properties" to Icons.Default.Home,
        "Tenants" to Icons.Default.People,
        "Payments" to Icons.Default.AttachMoney,
        "Requests" to Icons.Default.Build,
        "Messages" to Icons.Default.Message,
        "Settings" to Icons.Default.Settings
    )

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Profile Picture Section ---
        if (profileImageUrl.isNotEmpty()) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(100.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile",
                modifier = Modifier.size(100.dp),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { launcher.launch("image/*") }) {
            Text("Change Profile Picture")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Welcome, ${if (firstName.isNotEmpty()) firstName else "Landlord"} 👋",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Dashboard Icons ---
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(items) { (title, icon) ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable {
                            when (title) {
                                "Properties" -> navController.navigate("properties_screen")
                                "Tenants" -> navController.navigate("tenants_screen")
                                "Payments" -> navController.navigate("payments_screen")
                                "Requests" -> navController.navigate("requests_screen")
                                "Messages" -> navController.navigate("messages_screen")
                                "Settings" -> navController.navigate("settings_screen")
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, contentDescription = title, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(title, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Login.route)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Logout")
        }
    }
}

// --------------------- TENANT DASHBOARD ---------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TenantDashboardScreen(navController: NavHostController) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var firstName by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                firstName = doc.getString("firstName") ?: ""
            }
        }
    }

    val items = listOf(
        "My Property" to Icons.Default.Home,
        "Payments" to Icons.Default.AttachMoney,
        "Requests" to Icons.Default.Build,
        "Messages" to Icons.Default.Message,
        "Profile" to Icons.Default.Person,
        "Settings" to Icons.Default.Settings
    )

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome, ${if (firstName.isNotEmpty()) firstName else "Tenant"} 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(items) { (title, icon) ->
                Card(
                    modifier = Modifier.padding(8.dp).fillMaxWidth().aspectRatio(1f).clickable { },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, contentDescription = title, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(title, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(Screen.Login.route)
        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("Logout")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(navController: NavHostController) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var properties by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch landlord properties
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, "Error fetching properties", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        properties = snapshot.documents.mapNotNull { it.data }
                        isLoading = false
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Properties", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_property_screen") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Property")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (isLoading) {

                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            } else if (properties.isEmpty()) {

                // Centered message just like Tenant screen
                Text(
                    text = "You haven’t added any properties yet.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(properties) { property ->
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
                                    text = property["name"]?.toString() ?: "Unnamed Property",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Location: ${property["location"] ?: "N/A"}")
                                Text("Price: ${property["price"] ?: "N/A"}")
                                Text("Description: ${property["description"] ?: "No description"}")
                            }
                        }
                    }
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(navController: NavHostController) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val uploadedImageUrls = remember { mutableStateListOf<String>() }

    // Image picker launcher for multiple images
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.size > 5) {
            Toast.makeText(context, "You can upload up to 5 images only.", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        uris.forEach { uri ->
            val mimeType = context.contentResolver.getType(uri)
            if (mimeType?.startsWith("image/") != true) {
                Toast.makeText(context, "Only image files are allowed.", Toast.LENGTH_SHORT).show()
                return@forEach
            }

            val inputStream = context.contentResolver.openInputStream(uri)
            val fileSize = inputStream?.available() ?: 0
            if (fileSize > 5 * 1024 * 1024) { // 5MB
                Toast.makeText(context, "One of the images is too large! Max 5MB.", Toast.LENGTH_SHORT).show()
                return@forEach
            }

            val fileName = "properties/${System.currentTimeMillis()}_${uri.lastPathSegment}"
            val ref = storageRef.child(fileName)
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        uploadedImageUrls.add(url.toString())
                        Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Property", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Property Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (£)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text("Upload Property Image(s)")
            }

            if (uploadedImageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uploadedImageUrls.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Property Image",
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { uploadedImageUrls.remove(url) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (name.isNotEmpty() && location.isNotEmpty() && price.isNotEmpty() && uploadedImageUrls.isNotEmpty()) {
                        val property = hashMapOf(
                            "name" to name,
                            "location" to location,
                            "price" to price,
                            "description" to description,
                            "landlordId" to user?.uid,
                            "images" to uploadedImageUrls.toList()
                        )

                        db.collection("properties").add(property)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Property added!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Please fill all fields and upload at least one image.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Property")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var tenants by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch tenants for all landlord properties
    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { propertiesSnapshot ->
                    val propertyIds = propertiesSnapshot.documents.map { it.id }
                    if (propertyIds.isNotEmpty()) {
                        db.collection("tenants")
                            .whereIn("propertyId", propertyIds)
                            .get()
                            .addOnSuccessListener { tenantsSnapshot ->
                                tenants = tenantsSnapshot.documents.mapNotNull { it.data }
                                isLoading = false
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to load tenants: ${it.message}", Toast.LENGTH_SHORT).show()
                                isLoading = false
                            }
                    } else {
                        tenants = emptyList()
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load properties: ${it.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Tenants") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_tenant_screen") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Tenant")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (tenants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven’t added any tenants yet.")
                }
            } else {
                LazyColumn {
                    items(tenants) { tenant ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${tenant["firstName"]} ${tenant["lastName"]}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Email: ${tenant["email"]}")
                                Text("Phone: ${tenant["phone"]}")
                                Text("Property: ${tenant["propertyName"] ?: "N/A"}")
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedPropertyId by remember { mutableStateOf("") }
    var properties by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var errorMessage by remember { mutableStateOf("") }

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Load landlord properties
    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    properties = snapshot.documents.map { it.data!! + mapOf("id" to it.id) }
                }
        }
    }

    val emailPattern = Patterns.EMAIL_ADDRESS
    val phonePattern = Regex("^\\d{11}$")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Tenant", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") })
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") })
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })

            Spacer(modifier = Modifier.height(8.dp))

            // Property dropdown
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = properties.find { it["id"] == selectedPropertyId }?.get("name")?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("Select Property") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                when {
                    firstName.length < 3 -> { errorMessage = "First Name must be at least 3 characters."; return@Button }
                    lastName.length < 3 -> { errorMessage = "Last Name must be at least 3 characters."; return@Button }
                    !emailPattern.matcher(email).matches() -> { errorMessage = "Enter a valid email address."; return@Button }
                    !phonePattern.matches(phone) -> { errorMessage = "Phone number must be exactly 11 digits."; return@Button }
                    selectedPropertyId.isEmpty() -> { errorMessage = "Please select a property."; return@Button }
                }

                // Generate temporary password
                val tempPassword = "${firstName.lowercase()}${(1000..9999).random()}"

                auth.createUserWithEmailAndPassword(email, tempPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val tenantId = auth.currentUser?.uid ?: return@addOnCompleteListener

                            // Optional: send email verification
                            auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                                Toast.makeText(context, "Verification email sent to tenant!", Toast.LENGTH_SHORT).show()
                            }

                            val tenantData = hashMapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "email" to email,
                                "phone" to phone,
                                "propertyId" to selectedPropertyId,
                                "role" to "tenant",
                                "temporaryPassword" to tempPassword
                            )

                            db.collection("tenants").document(tenantId).set(tenantData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Tenant added successfully!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener { errorMessage = "Failed to save tenant info: ${it.message}" }
                        } else {
                            errorMessage = "Failed to create tenant: ${task.exception?.message}"
                        }
                    }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Add Tenant")
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}




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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var requests by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch requests for landlord
    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("requests")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    requests = snapshot.documents.map { it.data!! }
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load requests: ${it.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Requests", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (requests.isEmpty()) {
                    Text(
                        "No requests yet.",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(requests) { request ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Tenant: ${request["tenantName"] ?: "N/A"}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Property: ${request["propertyName"] ?: "N/A"}")
                                    Text("Request: ${request["description"] ?: "No description"}")
                                    Text("Status: ${request["status"] ?: "Pending"}")
                                    Text(
                                        "Date: ${
                                            (request["createdAt"] as? com.google.firebase.Timestamp)?.toDate()
                                                ?: "N/A"
                                        }"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch messages for the landlord
    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("messages")
                .whereEqualTo("landlordId", uid)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { snapshot ->
                    messages = snapshot.documents.mapNotNull { it.data }
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load messages: ${it.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                messages.isEmpty() -> {
                    Text(
                        "No messages yet.",
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
                        items(messages) { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "From: ${msg["senderName"] ?: "Unknown"}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Message: ${msg["content"] ?: ""}")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Date: ${msg["timestamp"]?.toString() ?: ""}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Fetch current profile image
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    profileImageUrl = doc.getString("profileImageUrl") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to load profile info", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val fileName = "profile_images/${user?.uid}.jpg"
            val ref = storageRef.child(fileName)
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        profileImageUrl = url.toString()
                        db.collection("users").document(user!!.uid)
                            .update("profileImageUrl", profileImageUrl)
                        Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Profile Picture ---
                if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(100.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Profile",
                        modifier = Modifier.size(100.dp),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { launcher.launch("image/*") }) {
                    Text("Change Profile Picture")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Editable Name Fields ---
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Password Change ---
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        errorMessage = ""

                        // Update name
                        if (firstName.isNotBlank() || lastName.isNotBlank()) {
                            val updates = hashMapOf<String, Any>()
                            if (firstName.isNotBlank()) updates["firstName"] = firstName
                            if (lastName.isNotBlank()) updates["lastName"] = lastName

                            user?.uid?.let { uid ->
                                db.collection("users").document(uid).update(updates)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Name updated!", Toast.LENGTH_SHORT).show()
                                        firstName = ""
                                        lastName = ""
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Failed to update name: ${e.message}"
                                    }
                            }
                        }

                        // Update password
                        if (newPassword.isNotBlank()) {
                            user?.updatePassword(newPassword)
                                ?.addOnSuccessListener {
                                    Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                                    newPassword = ""
                                }
                                ?.addOnFailureListener {
                                    errorMessage = "Failed to update password: ${it.message}"
                                }
                        }

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}


