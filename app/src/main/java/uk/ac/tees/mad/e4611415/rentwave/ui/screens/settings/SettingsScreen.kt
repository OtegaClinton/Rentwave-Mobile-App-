package uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
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
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            /* 🔹 ACCOUNT */
            Text("Account", style = MaterialTheme.typography.titleMedium)

            SettingsItem(
                icon = Icons.Default.Edit,
                title = "Edit Profile",
                onClick = {
                    navController.navigate(Screen.EditProfile.route)
                }
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Change Password",
                onClick = {
                    navController.navigate(Screen.ChangePassword.route)
                }
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
                onClick = {
                    navController.navigate(Screen.About.route)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            /* 🔴 LOGOUT */
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
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
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}
