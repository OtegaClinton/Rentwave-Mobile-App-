package uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTenantRequestScreen(
    navController: NavHostController,
    requestId: String
) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // ✅ Mutable list that supports add/remove
    val images = remember { mutableStateListOf<String>() }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    /* ---------- LOAD REQUEST ---------- */
    LaunchedEffect(requestId) {
        db.collection("tenant_requests")
            .document(requestId)
            .get()
            .addOnSuccessListener { doc ->
                title = doc.getString("title") ?: ""
                description = doc.getString("description") ?: ""

                images.clear()
                val existingImages = doc["images"] as? List<*> ?: emptyList<Any>()
                images.addAll(existingImages.mapNotNull { it?.toString() })

                loading = false
            }
            .addOnFailureListener {
                loading = false
                Toast.makeText(context, "Failed to load request", Toast.LENGTH_SHORT).show()
            }
    }

    /* ---------- GALLERY IMAGE PICKER ---------- */
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            val ref = storageRef.child("request_images/${System.currentTimeMillis()}.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        images.add(url.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Request", color = Color.White) },
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

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            /* ---------- IMAGE PREVIEW + REMOVE ---------- */
            if (images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(images) { img ->
                        Box {
                            AsyncImage(
                                model = img,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp)
                            )

                            IconButton(
                                onClick = { images.remove(img) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Images")
            }

            /* ---------- SAVE CHANGES ---------- */
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    saving = true

                    db.collection("tenant_requests")
                        .document(requestId)
                        .update(
                            mapOf(
                                "title" to title,
                                "description" to description,
                                "images" to images.toList()
                            )
                        )
                        .addOnSuccessListener {
                            saving = false
                            Toast.makeText(context, "Request updated", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            saving = false
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            /* ---------- DELETE REQUEST ---------- */
            Button(
                onClick = {
                    db.collection("tenant_requests")
                        .document(requestId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Request deleted", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(6.dp))
                Text("Delete Request")
            }
        }
    }
}
