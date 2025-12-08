package uk.ac.tees.mad.e4611415.rentwave.ui.screens.dashboard

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandlordDashboardScreen(navController: NavHostController) {

    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }

    // Fetch landlord details
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    firstName = doc.getString("firstName") ?: ""
                    profileImageUrl = doc.getString("profileImageUrl") ?: ""
                }
        }
    }

    // Image Picker for profile image
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

                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val items = listOf(
        Triple("Properties", Icons.Default.Home, Screen.Properties.route),
        Triple("Tenants", Icons.Default.People, Screen.Tenants.route),
        Triple("Payments", Icons.Default.AttachMoney, Screen.Payments.route),
        Triple("Requests", Icons.Default.Build, Screen.Requests.route),
        Triple("Messages", Icons.Default.Message, Screen.Messages.route),
        Triple("Settings", Icons.Default.Settings, Screen.Settings.route)
    )

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Profile Image
        if (profileImageUrl.isNotEmpty()) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile",
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

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Welcome, ${if (firstName.isNotBlank()) firstName else "Landlord"} 👋",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Dashboard tiles
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(items) { (title, icon, route) ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable { navController.navigate(route) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, null, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(title)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.LandlordDashboard.route) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Logout")
        }
    }
}
