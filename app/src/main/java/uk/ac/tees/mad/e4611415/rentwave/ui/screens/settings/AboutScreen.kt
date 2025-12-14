package uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.ac.tees.mad.e4611415.rentwave.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About RentWave", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /*  BRANDING */
            Image(
                painter = painterResource(id = R.drawable.rentwave_logo),
                contentDescription = "RentWave Logo",
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "RentWave",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Smart Rental Management",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Text(
                "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            /* MAIN CONTENT CARD */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    SectionHeader("About the App")
                    Text(
                        """
                        RentWave is a modern rental management platform built to simplify 
                        rent payments, maintenance requests, and communication between 
                        landlords and tenants.

                        The app focuses on usability, security, and real-time updates 
                        to deliver a seamless rental experience.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Divider()

                    SectionHeader("Key Features")
                    Bullet("Secure rent payments and tracking")
                    Bullet("Maintenance requests with image uploads")
                    Bullet("Real-time tenant & landlord dashboards")
                    Bullet("Cloud-backed authentication & storage")

                    Divider()

                    SectionHeader("Developer")
                    Text("Oghenetega Clinton Okotie", fontWeight = FontWeight.Medium)
                    Text("Teesside University")
                    Text("MSc Computer Science")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "© 2025 RentWave • All rights reserved",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/* HELPERS */

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Bullet(text: String) {
    Text("• $text", style = MaterialTheme.typography.bodyMedium)
}
