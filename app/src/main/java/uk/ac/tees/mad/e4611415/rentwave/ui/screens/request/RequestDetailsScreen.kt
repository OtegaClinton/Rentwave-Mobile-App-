package uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantRequestDetailsScreen(
    navController: NavHostController,
    requestId: String
) {

    val db = FirebaseFirestore.getInstance()

    var request by remember { mutableStateOf<Map<String, Any>?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(requestId) {
        db.collection("tenant_requests")
            .document(requestId)
            .get()
            .addOnSuccessListener {
                request = it.data
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Details", color = Color.White) },
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

        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            request == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Request not found")
            }

            else -> {

                val formatter =
                    SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())

                val date = (request!!["timestamp"] as? Timestamp)?.toDate()
                val formattedDate = date?.let { formatter.format(it) } ?: "Unknown"

                val status = request!!["status"].toString()
                val images = request!!["images"] as? List<*> ?: emptyList<Any>()

                Card(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        /* TITLE */
                        Text(
                            request!!["title"].toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Submitted on $formattedDate",
                            color = Color.Gray
                        )

                        Divider()

                        /* DESCRIPTION */
                        SectionTitle("Issue Description")
                        Text(request!!["description"].toString())

                        Divider()

                        /* IMAGES */
                        if (images.isNotEmpty()) {
                            SectionTitle("Images")

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(images) { img ->
                                    Image(
                                        painter = rememberAsyncImagePainter(img.toString()),
                                        contentDescription = null,
                                        modifier = Modifier.size(140.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Divider()
                        }

                        /* 🔹 STATUS (READ ONLY) */
                        SectionTitle("Status")
                        StatusChip(status)
                    }
                }
            }
        }
    }
}


@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "resolved" -> Color(0xFF2E7D32)
        "in progress" -> Color(0xFFFF8F00)
        else -> Color(0xFFD32F2F)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
