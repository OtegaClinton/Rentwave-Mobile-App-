package uk.ac.tees.mad.e4611415.rentwave.ui.screens.properties

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsScreen(navController: NavHostController, propertyId: String) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var property by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteLoading by remember { mutableStateOf(false) }

    LaunchedEffect(propertyId) {
        db.collection("properties").document(propertyId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    property = doc.data
                } else {
                    Toast.makeText(context, "Property not found", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load property details", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Property Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        property?.let { details ->

            val rawImages = details["images"]
            val images = when (rawImages) {
                is List<*> -> rawImages.mapNotNull { it?.toString() }
                is String -> listOf(rawImages)
                else -> emptyList()
            }

            val listState = rememberLazyListState()
            val currentIndex by remember {
                derivedStateOf { listState.firstVisibleItemIndex.coerceAtLeast(0) }
            }

            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔹 Safe Image Carousel Handling
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
                                contentDescription = "Property Image",
                                modifier = Modifier.fillParentMaxWidth().fillMaxHeight()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(images.size) { index ->
                            val color = if (index == currentIndex)
                                MaterialTheme.colorScheme.primary else Color.LightGray

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(3.dp)
                                    .background(color, CircleShape)
                            )
                        }
                    }
                } else {
                    // 🛑 Placeholder for NO IMAGES
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = details["name"]?.toString() ?: "Unnamed Property",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "£${details["price"] ?: "N/A"} / month",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Location: ${details["location"] ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = details["description"]?.toString() ?: "No description added.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { navController.navigate(Screen.EditProperty.passId(propertyId)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text("Edit Property")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Property")
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Property") },
            text = { Text("Are you sure you want to delete this property?") },
            confirmButton = {
                if (deleteLoading) {
                    CircularProgressIndicator()
                } else {
                    TextButton(
                        onClick = {
                            deleteLoading = true
                            db.collection("properties").document(propertyId)
                                .delete()
                                .addOnSuccessListener {
                                    deleteLoading = false
                                    Toast.makeText(context, "Property deleted ✔", Toast.LENGTH_SHORT).show()
                                    showDeleteDialog = false
                                    navController.navigate(Screen.Properties.route) {
                                        popUpTo(Screen.PropertyDetails.route) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    deleteLoading = false
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                }
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
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
