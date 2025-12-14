package uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantSettingsScreen(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
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

            Text("Support", style = MaterialTheme.typography.titleMedium)

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About App",
                onClick = {
                    navController.navigate(Screen.About.route)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.TenantDashboard.route) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}
