package uk.ac.tees.mad.e4611415.rentwave.ui.screens.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(navController: NavHostController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "Welcome to Rentwave!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = """
                By using our platform, you agree to the following:

                1️⃣ Account Use  
                You must provide accurate information when creating an account.

                2️⃣ Property Listings  
                Landlords are responsible for the accuracy and legality of listings.

                3️⃣ Privacy & Data  
                We respect your privacy. Your information is protected and used only for rental activities.

                4️⃣ Communication  
                You consent to receive essential updates related to your account.

                5️⃣ Prohibited Activities  
                Illegal, fraudulent, or abusive behavior is not allowed.

                6️⃣ Modifications  
                Terms may be updated when needed — we will notify users of key changes.

                7️⃣ Liability  
                Rentwave is not responsible for disputes between tenants and landlords.

                8️⃣ Governing Law  
                These terms follow applicable laws in your region.

                © 2025 Rentwave — All Rights Reserved.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        }
    }
}
