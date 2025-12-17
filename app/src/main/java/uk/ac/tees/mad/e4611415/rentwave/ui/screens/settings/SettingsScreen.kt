package uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var showDeactivateDialog by remember { mutableStateOf(false) }
    var deactivating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
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
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            /* 🔹 ACCOUNT */
            Text("Account", style = MaterialTheme.typography.titleMedium)

            SettingsItem(
                icon = Icons.Default.Edit,
                title = "Edit Profile",
                onClick = { navController.navigate(Screen.EditProfile.route) }
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Change Password",
                onClick = { navController.navigate(Screen.ChangePassword.route) }
            )

            Divider()

            /* 🔹 PREFERENCES */
            Text("Preferences", style = MaterialTheme.typography.titleMedium)

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                onClick = {
                    Toast.makeText(
                        context,
                        "Notifications coming soon 🚧",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            Divider()

            /* 🔹 SUPPORT */
            Text("Support", style = MaterialTheme.typography.titleMedium)

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About App",
                onClick = { navController.navigate(Screen.About.route) }
            )

            Spacer(modifier = Modifier.weight(1f))

            /* 🔴 LOGOUT */
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.LandlordDashboard.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }

            /* ⚠️ DEACTIVATE ACCOUNT */
            OutlinedButton(
                onClick = { showDeactivateDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Deactivate Account")
            }
        }
    }

    /* 🔥 DEACTIVATION CONFIRMATION */
    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { if (!deactivating) showDeactivateDialog = false },
            title = { Text("Deactivate Account") },
            text = {
                Text(
                    "This will deactivate your account and log you out.\n\n" +
                            "You can contact support to reactivate it later."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val user = auth.currentUser ?: return@TextButton
                        deactivating = true

                        db.collection("users")
                            .document(user.uid)
                            .update("isActive", false)
                            .addOnSuccessListener {
                                auth.signOut()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0)
                                }
                            }
                            .addOnFailureListener {
                                deactivating = false
                                Toast.makeText(
                                    context,
                                    "Failed to deactivate account",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                ) {
                    if (deactivating)
                        CircularProgressIndicator(Modifier.size(18.dp))
                    else
                        Text("Deactivate", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
