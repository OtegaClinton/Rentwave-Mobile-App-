package uk.ac.tees.mad.e4611415.rentwave.ui.screens.properties

// Required imports
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPropertyScreen(navController: NavHostController, propertyId: String) {

    // Firebase & UI references
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val coroutineScope = rememberCoroutineScope()

    // Form states
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Image list (can remove images)
    val uploadedImages = remember { mutableStateListOf<String>() }

    // Loading states
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Load existing property data
    LaunchedEffect(propertyId) {
        db.collection("properties").document(propertyId)
            .get()
            .addOnSuccessListener { doc ->
                val data = doc.data
                if (data != null) {
                    name = data["name"]?.toString() ?: ""
                    location = data["location"]?.toString() ?: ""
                    price = data["price"]?.toString() ?: ""
                    description = data["description"]?.toString() ?: ""
                    val imgs = data["images"] as? List<String> ?: emptyList()
                    uploadedImages.clear()
                    uploadedImages.addAll(imgs)
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Error loading property", Toast.LENGTH_SHORT).show()
            }
    }

    // Launcher for uploading new images
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uploadedImages.size + uris.size > 5) {
            Toast.makeText(context, "Max 5 images allowed total", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        isSaving = true
        uris.forEach { uri ->
            val ref = storage.reference.child("properties/${System.currentTimeMillis()}_${uri.lastPathSegment}")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        uploadedImages.add(url.toString())
                        isSaving = false
                    }
                }
                .addOnFailureListener {
                    isSaving = false
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Property", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { pad ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Editable fields
            OutlinedTextField(name, { name = it }, label = { Text("Property Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                price, { price = it },
                label = { Text("Price (£)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                description, { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(Modifier.height(16.dp))

            // Image preview + delete tap
            if (uploadedImages.isNotEmpty()) {
                LazyRow {
                    items(uploadedImages) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Property Image",
                            modifier = Modifier
                                .size(100.dp)
                                .padding(4.dp)
                                .clickable { uploadedImages.remove(url) } // remove on tap
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Upload more images button
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                ) else Text("Add More Images")
            }

            Spacer(Modifier.height(25.dp))

            // SAVE UPDATED PROPERTY BUTTON
            Button(
                onClick = {
                    if (name.isBlank() || location.isBlank() || price.isBlank()) {
                        Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true

                    val updatedData = mapOf(
                        "name" to name,
                        "location" to location,
                        "price" to price,
                        "description" to description,
                        "images" to uploadedImages.toList()
                    )

                    // Update Firestore document
                    db.collection("properties").document(propertyId)
                        .update(updatedData)
                        .addOnSuccessListener {
                            isSaving = false
                            Toast.makeText(context, "Property updated ✔", Toast.LENGTH_SHORT).show()

                            // Go back to details screen
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            isSaving = false
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                ) else Text("Save Changes")
            }
        }
    }
}
