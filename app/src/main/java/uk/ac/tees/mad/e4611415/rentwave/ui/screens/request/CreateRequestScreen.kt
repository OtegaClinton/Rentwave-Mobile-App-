package uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Pick up to 2 images
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.size <= 2) {
                selectedImages = uris
            } else {
                Toast.makeText(context, "You can upload only 2 images", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Request", color = Color.White) },
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Request Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Text("Upload Images (optional, max 2)", style = MaterialTheme.typography.bodyMedium)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                    }
                }

                items(selectedImages.size) { index ->
                    Image(
                        painter = rememberAsyncImagePainter(selectedImages[index]),
                        contentDescription = "Selected Image",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Please fill in title and description", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true

                    scope.launch {
                        val uploadedImages = mutableListOf<String>()

                        // Upload ONLY if images were selected
                        if (selectedImages.isNotEmpty()) {
                            for ((i, uri) in selectedImages.withIndex()) {
                                try {
                                    val ref = storage.child("request_images/${userId}_${System.currentTimeMillis()}_$i.jpg")
                                    ref.putFile(uri).await()
                                    val url = ref.downloadUrl.await().toString()
                                    uploadedImages.add(url)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        // Build request object
                        val requestData = mapOf(
                            "userId" to userId,
                            "title" to title,
                            "description" to description,
                            "images" to uploadedImages,       // <-- empty list if none
                            "timestamp" to Timestamp.now(),
                            "status" to "Pending"
                        )

                        firestore.collection("tenant_requests")
                            .add(requestData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Request submitted!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to submit request", Toast.LENGTH_SHORT).show()
                            }

                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading)
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else
                    Text("Submit Request")
            }
        }
    }
}
