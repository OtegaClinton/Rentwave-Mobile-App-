package uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {

    val user = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var profileImageUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    // Image Picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val ref = storage.child("profile_images/${user.uid}.jpg")
                    ref.putFile(uri).await()
                    val url = ref.downloadUrl.await().toString()
                    profileImageUrl = url

                    db.collection("users").document(user.uid)
                        .update("profileImageUrl", url)

                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Load user data
    LaunchedEffect(Unit) {
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                firstName = doc.getString("firstName") ?: ""
                lastName = doc.getString("lastName") ?: ""
                profileImageUrl = doc.getString("profileImageUrl") ?: ""
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->

        if (loading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Profile Image + Change Icon
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {

                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(120.dp)
                )

                IconButton(onClick = { launcher.launch("image/*") }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change Picture", tint = Color.White)
                }
            }

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
                enabled = false,
                onValueChange = {},
                label = { Text("Email (Read Only)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    saving = true
                    db.collection("users").document(user.uid)
                        .update(
                            mapOf(
                                "firstName" to firstName,
                                "lastName" to lastName
                            )
                        )
                        .addOnSuccessListener {
                            saving = false
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            saving = false
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (saving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Save Changes")
            }
        }
    }
}
