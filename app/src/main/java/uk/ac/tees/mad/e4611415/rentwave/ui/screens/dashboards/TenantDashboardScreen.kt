package uk.ac.tees.mad.e4611415.rentwave.ui.screens.dashboard

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4611415.rentwave.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDashboardScreen(navController: NavHostController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var showProfileOptions by remember { mutableStateOf(false) }

    val unreadNotifications = 3

    /* ---------- IMAGE PICKER ---------- */

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val uid = user?.uid ?: return@rememberLauncherForActivityResult

        val imageRef = storageRef.child("profile_images/$uid/profile.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { url ->
                    profileImageUrl = url.toString()
                    db.collection("users")
                        .document(uid)
                        .update("profileImageUrl", profileImageUrl)

                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    /* ---------- FETCH USER DATA ---------- */

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    firstName = doc.getString("firstName") ?: ""
                    profileImageUrl = doc.getString("profileImageUrl") ?: ""
                }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            TenantDrawerContent(navController, drawerState, firstName, profileImageUrl)
        }
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Menu", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null, tint = Color.White)
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = {
                                Toast.makeText(context, "Notifications coming soon…", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Notifications, null, tint = Color.White)
                            }

                            if (unreadNotifications > 0) {
                                Badge(
                                    containerColor = Color.Red,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(unreadNotifications.toString(), color = Color.White)
                                }
                            }
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
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(12.dp))

                /* ✅ CIRCULAR PROFILE IMAGE */
                if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable { showProfileOptions = true }
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { showProfileOptions = true }
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Welcome, ${if (firstName.isNotBlank()) firstName else "Tenant"} 👋",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(16.dp))

                val items = listOf(
                    Triple("My Rent", Icons.Default.Home, Screen.MyRent.route),
                    Triple("Payments", Icons.Default.AttachMoney, Screen.TenantPaymentsHome.route),
                    Triple("Requests", Icons.Default.Build, Screen.TenantRequestsHome.route),
                    Triple("Messages", Icons.Default.Message, Screen.TenantMessages.route),
                    Triple("Profile", Icons.Default.Person, Screen.TenantProfile.route),
                    Triple("Settings", Icons.Default.Settings, Screen.TenantSettings.route)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxHeight(),
                    userScrollEnabled = false
                ) {
                    items(items) { (title, icon, route) ->
                        TenantTile(title, icon, route, navController)
                    }
                }
            }
        }
    }

    /* ---------- PROFILE OPTIONS ---------- */

    if (showProfileOptions) {
        AlertDialog(
            onDismissRequest = { showProfileOptions = false },
            title = { Text("Profile Options") },
            confirmButton = {
                TextButton(onClick = {
                    showProfileOptions = false
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("imageUrl", profileImageUrl)
                    navController.navigate(Screen.ViewProfileImage.route)
                }) {
                    Text("View Profile Image")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileOptions = false
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Change Picture")
                }
            }
        )
    }
}

/* ---------- TILE ---------- */

@Composable
fun TenantTile(
    title: String,
    icon: ImageVector,
    route: String,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(150.dp),
        onClick = { navController.navigate(route) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(10.dp))
            Text(title)
        }
    }
}

/* ---------- DRAWER ---------- */

@Composable
fun TenantDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    firstName: String,
    profileImageUrl: String
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(modifier = Modifier.width(260.dp)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ✅ CIRCULAR DRAWER IMAGE */
            if (profileImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    null,
                    tint = Color.Gray,
                    modifier = Modifier.size(90.dp)
                )
            }

            Text(
                "Hi, ${if (firstName.isNotBlank()) firstName else "Tenant"}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Divider()

        DrawerItem("My Rent", Icons.Default.Home, navController, Screen.MyRent.route, drawerState)
        DrawerItem("Payments", Icons.Default.AttachMoney, navController, Screen.TenantPaymentsHome.route, drawerState)
        DrawerItem("Requests", Icons.Default.Build, navController, Screen.TenantRequestsHome.route, drawerState)
        DrawerItem("Messages", Icons.Default.Message, navController, Screen.TenantMessages.route, drawerState)
        DrawerItem("Profile", Icons.Default.Person, navController, Screen.TenantProfile.route, drawerState)
        DrawerItem("Settings", Icons.Default.Settings, navController, Screen.TenantSettings.route, drawerState)

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        DrawerItem(
            "Logout",
            Icons.Default.ExitToApp,
            navController,
            Screen.Login.route,
            drawerState,
            labelColor = Color.Red,
            iconColor = Color.Red
        ) {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.TenantDashboard.route) { inclusive = true }
            }
        }
    }
}
