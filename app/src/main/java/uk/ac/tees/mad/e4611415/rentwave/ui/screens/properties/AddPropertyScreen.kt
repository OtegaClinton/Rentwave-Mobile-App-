package uk.ac.tees.mad.e4611415.rentwave.ui.screens.properties

// Required imports
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(navController: NavHostController) {

    // Get Firebase and UI context
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference

    // Form states
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // List of uploaded image URLs
    val uploadedImageUrls = remember { mutableStateListOf<String>() }

    // Loading states for buttons (spinner control)
    var isUploading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Select multiple images from gallery
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->

        // Limit image upload to max 5
        if (uris.size > 5) {
            Toast.makeText(context, "Max 5 images allowed", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        // Start loading spinner while uploading
        isUploading = true

        // Upload each selected image
        uris.forEachIndexed { index, uri ->
            val fileName = "properties/${System.currentTimeMillis()}_${uri.lastPathSegment}"
            val ref = storageRef.child(fileName)

            // Upload to Firebase Storage
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        uploadedImageUrls.add(url.toString())

                        // When last image is uploaded
                        if (index == uris.lastIndex) {
                            isUploading = false
                            Toast.makeText(context, "Image(s) uploaded ✔", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    isUploading = false
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // UI Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Property", color = MaterialTheme.colorScheme.onPrimary) },

                // Back navigation
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
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Form Fields ---

            OutlinedTextField(name, { name = it }, label = { Text("Property Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                price,
                { price = it },
                label = { Text("Price (£)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                description,
                { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(Modifier.height(16.dp))

            // Upload Image Button with Spinner
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading && !isSaving
            ) {
                if (isUploading) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                ) else Text("Upload Images (Optional)")
            }

            // Display uploaded images
            if (uploadedImageUrls.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row {
                    uploadedImageUrls.forEach {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).padding(4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(25.dp))

            // Save Property Button with Spinner
            Button(
                onClick = {

                    // Simple validation
                    if (name.isEmpty() || location.isEmpty() || price.isEmpty()) {
                        isSaving = false
                        Toast.makeText(context, "Fill required fields!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Start spinner
                    isSaving = true

                    // Create property data object
                    val property = mapOf(
                        "name" to name,
                        "location" to location,
                        "price" to price,
                        "description" to description,
                        "landlordId" to user?.uid,
                        "images" to uploadedImageUrls.toList()
                    )

                    // Save to Firestore
                    db.collection("properties").add(property)
                        .addOnSuccessListener {
                            isSaving = false
                            Toast.makeText(context, "Property Saved ✔", Toast.LENGTH_SHORT).show()

                            // Navigate to Properties List screen
                            navController.navigate(Screen.Properties.route) {
                                popUpTo(Screen.AddProperty.route) { inclusive = true } // Option A ✔
                                launchSingleTop = true
                            }
                        }
                        .addOnFailureListener {
                            isSaving = false
                            Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                ) else Text("Save Property")
            }
        }
    }
}
