package uk.ac.tees.mad.e4611415.rentwave.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantProfileScreen(navController: NavHostController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    var profileImage by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var property by remember { mutableStateOf("") }
    var rentAmount by remember { mutableStateOf("") }
    var landlordName by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var showImageDialog by remember { mutableStateOf(false) }

    // Pick image launcher
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val ref = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    profileImage = url.toString()
                    db.collection("users").document(userId)
                        .update("profileImageUrl", profileImage)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    // Load data
    LaunchedEffect(Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                name = "${doc.get("firstName")} ${doc.get("lastName")}"
                phone = doc.getString("phone") ?: ""
                property = doc.getString("propertyName") ?: ""
                rentAmount = doc.getString("rentAmount") ?: ""
                profileImage = doc.getString("profileImageUrl") ?: ""

                val landlordId = doc.getString("landlordId")
                if (!landlordId.isNullOrEmpty()) {
                    db.collection("users").document(landlordId).get()
                        .addOnSuccessListener { landlordDoc ->
                            landlordName = "${landlordDoc.get("firstName")} ${landlordDoc.get("lastName")}"
                        }
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profile Picture Section
            Box(contentAlignment = Alignment.Center) {
                if (profileImage.isNotEmpty()) {
                    AsyncImage(
                        model = profileImage,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { showImageDialog = true }
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.4f))
                            .clickable { showImageDialog = true }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(email, color = Color.Gray)

            Spacer(Modifier.height(20.dp))

            ProfileInfoCard("Phone", phone)
            ProfileInfoCard("Property", property)
            ProfileInfoCard("Rent Amount", "£$rentAmount")
            ProfileInfoCard("Landlord", landlordName)

            Spacer(Modifier.height(25.dp))

            // Change Password button
//            OutlinedButton(
//                onClick = {
//                    auth.sendPasswordResetEmail(email)
//                    Toast.makeText(context, "Password reset link sent to your email.", Toast.LENGTH_SHORT).show()
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Change Password")
//            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.TenantDashboard.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }

    // Popup dialog for profile image actions
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Profile Picture") },
            text = { Text("Choose an option:") },
            confirmButton = {
                TextButton(onClick = {
                    showImageDialog = false
                    navController.navigate(Screen.ViewProfileImage.passUrl(profileImage))
                }) { Text("View Image") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageDialog = false
                    imagePicker.launch("image/*")
                }) { Text("Change Picture") }
            }
        )
    }
}

@Composable
fun ProfileInfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F4)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
    }
}
