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
fun LandlordDashboardScreen(navController: NavHostController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var showProfileOptions by remember { mutableStateOf(false) }

    val notiCount = 3

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
            DrawerContent(navController, drawerState, firstName, profileImageUrl)
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
                                Toast.makeText(context, "Notifications coming soon...", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Notifications, null, tint = Color.White)
                            }

                            if (notiCount > 0) {
                                Badge(
                                    containerColor = Color.Red,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(notiCount.toString(), color = Color.White)
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

                if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)          // ✅ ONLY CHANGE
                            .clickable { showProfileOptions = true }
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { showProfileOptions = true }
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Welcome, ${if (firstName.isNotBlank()) firstName else "Landlord"} 👋",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(16.dp))

                val items = listOf(
                    Triple("Properties", Icons.Default.Home, Screen.Properties.route),
                    Triple("Tenants", Icons.Default.People, Screen.Tenants.route),
                    Triple("Payments", Icons.Default.AttachMoney, Screen.Payments.route),
                    Triple("Requests", Icons.Default.Build, Screen.Requests.route),
                    Triple("Messages", Icons.Default.Message, Screen.Messages.route),
                    Triple("Settings", Icons.Default.Settings, Screen.Settings.route)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxHeight(),
                    userScrollEnabled = false
                ) {
                    items(items) { (title, icon, route) ->
                        DashboardTile(title, icon, route, navController)
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
                }) { Text("Preview Image") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileOptions = false
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("Change Picture") }
            }
        )
    }
}

/* ---------- DASHBOARD TILE ---------- */

@Composable
fun DashboardTile(
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
fun DrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    firstName: String,
    profileImageUrl: String
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(modifier = Modifier.width(260.dp)) {

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (profileImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)      // ✅ ONLY CHANGE
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
                "Hi, ${if (firstName.isNotBlank()) firstName else "Landlord"}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Divider()

        DrawerItem("Properties", Icons.Default.Home, navController, Screen.Properties.route, drawerState)
        DrawerItem("Tenants", Icons.Default.People, navController, Screen.Tenants.route, drawerState)
        DrawerItem("Payments", Icons.Default.AttachMoney, navController, Screen.Payments.route, drawerState)
        DrawerItem("Requests", Icons.Default.Build, navController, Screen.Requests.route, drawerState)
        DrawerItem("Messages", Icons.Default.Message, navController, Screen.Messages.route, drawerState)
        DrawerItem("Settings", Icons.Default.Settings, navController, Screen.Settings.route, drawerState)

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
                popUpTo(Screen.LandlordDashboard.route) { inclusive = true }
            }
        }
    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: ImageVector,
    navController: NavHostController,
    route: String,
    drawerState: DrawerState,
    labelColor: Color = LocalContentColor.current,
    iconColor: Color = LocalContentColor.current,
    onClickOverride: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = iconColor) },
        label = { Text(title, color = labelColor) },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            onClickOverride?.invoke() ?: navController.navigate(route)
        }
    )
}

