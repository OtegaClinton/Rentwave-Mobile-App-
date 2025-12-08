package uk.ac.tees.mad.e4611415.rentwave.ui.screens.dashboard

import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TenantDashboardScreen(navController: NavHostController) {

    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var firstName by remember { mutableStateOf("") }

    // Fetch tenant name
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                firstName = doc.getString("firstName") ?: ""
            }
        }
    }

    // Tenant dashboard menu options
    val items = listOf(
        "My Rent" to Icons.Default.Home,
        "Payments" to Icons.Default.CurrencyPound, // £ icon
        "Requests" to Icons.Default.Build,
        "Messages" to Icons.Default.Message,
        "Notifications" to Icons.Default.Notifications,
        "Profile" to Icons.Default.Person,
        "Settings" to Icons.Default.Settings
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Welcome, ${if (firstName.isNotEmpty()) firstName else "Tenant"} 👋",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(items) { (title, icon) ->

                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable {
                            when (title) {
                                "My Rent" -> Toast.makeText(
                                    navController.context,
                                    "Coming Soon...",
                                    Toast.LENGTH_SHORT
                                ).show()

                                "Payments" -> navController.navigate(Screen.Payments.route)
                                "Requests" -> navController.navigate(Screen.Requests.route)
                                "Messages" -> navController.navigate(Screen.Messages.route)
                                "Notifications" ->
                                    Toast.makeText(navController.context,
                                        "Notifications coming soon",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                "Profile" ->
                                    Toast.makeText(navController.context,
                                        "Profile coming soon",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                "Settings" -> navController.navigate(Screen.Settings.route)
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
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
