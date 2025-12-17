package uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantRequestDetailsScreen(
    navController: NavHostController,
    requestId: String
) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var request by remember { mutableStateOf<Map<String, Any>?>(null) }
    var loading by remember { mutableStateOf(true) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteLoading by remember { mutableStateOf(false) }

    LaunchedEffect(requestId) {
        db.collection("tenant_requests")
            .document(requestId)
            .get()
            .addOnSuccessListener {
                request = it.data
                loading = false
            }
            .addOnFailureListener {
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
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            request == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Request not found")
            }

            else -> {
                val ownerId = request!!["userId"]?.toString()
                val isOwner = (currentUserId != null && currentUserId == ownerId)

                val formatter = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
                val date = (request!!["timestamp"] as? Timestamp)?.toDate()
                val formattedDate = date?.let { formatter.format(it) } ?: "Unknown"

                val status = request!!["status"]?.toString() ?: "Pending"

                val rawImages = request!!["images"]
                val images = when (rawImages) {
                    is List<*> -> rawImages.mapNotNull { it?.toString() }
                    is String -> listOf(rawImages)
                    else -> emptyList()
                }

                val listState = rememberLazyListState()
                val currentIndex by remember {
                    derivedStateOf { listState.firstVisibleItemIndex.coerceAtLeast(0) }
                }

                Card(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        // IMAGE CAROUSEL
                        if (images.isNotEmpty()) {
                            LazyRow(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            ) {
                                itemsIndexed(images) { _, img ->
                                    AsyncImage(
                                        model = img,
                                        contentDescription = "Request Image",
                                        modifier = Modifier
                                            .fillParentMaxWidth()
                                            .fillMaxHeight()
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(images.size) { index ->
                                    val color =
                                        if (index == currentIndex)
                                            MaterialTheme.colorScheme.primary
                                        else Color.LightGray

                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .padding(3.dp)
                                            .background(color, CircleShape)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No Images Available", color = Color.DarkGray)
                            }
                        }

                        Text(
                            text = request!!["title"]?.toString() ?: "No Title",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text("Submitted on $formattedDate", color = Color.Gray)

                        Divider()

                        Text("Description", fontWeight = FontWeight.Bold)
                        Text(request!!["description"]?.toString() ?: "")

                        Divider()

                        Text("Status", fontWeight = FontWeight.Bold)
                        StatusChip(status)

                        if (isOwner) {
                            Spacer(Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    navController.navigate(Screen.EditTenantRequest.passId(requestId))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Edit Request")
                            }

                            Spacer(Modifier.height(10.dp))

                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Delete Request")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!deleteLoading) showDeleteDialog = false },
            title = { Text("Delete Request") },
            text = { Text("Are you sure you want to delete this request?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteLoading = true
                        FirebaseFirestore.getInstance()
                            .collection("tenant_requests")
                            .document(requestId)
                            .delete()
                            .addOnSuccessListener {
                                deleteLoading = false
                                showDeleteDialog = false
                                Toast.makeText(context, "Request deleted", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                deleteLoading = false
                                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                            }
                    }
                ) {
                    if (deleteLoading)
                        CircularProgressIndicator(Modifier.size(18.dp))
                    else
                        Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
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
