package uk.ac.tees.mad.e4611415.rentwave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import uk.ac.tees.mad.e4611415.rentwave.navigation.AppNavigation
import uk.ac.tees.mad.e4611415.rentwave.ui.theme.RentwaveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            RentwaveTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
