package uk.ac.tees.mad.e4611415.rentwave.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.auth.*
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.dashboard.*
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.properties.*
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.tenants.*
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments.PaymentsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests.RequestsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.messages.MessagesScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Authentication
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.SignUp.route) { SignUpScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.Terms.route) { TermsScreen(navController) }

        // Dashboards
        composable(Screen.LandlordDashboard.route) { LandlordDashboardScreen(navController) }
        composable(Screen.TenantDashboard.route) { TenantDashboardScreen(navController) }

        // Properties Management
        composable(Screen.Properties.route) { PropertiesScreen(navController) }
        composable(Screen.AddProperty.route) { AddPropertyScreen(navController) }
        composable(Screen.EditProperty.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("propertyId") ?: ""
            EditPropertyScreen(navController, id)
        }
        composable(Screen.PropertyDetails.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("propertyId") ?: ""
            PropertyDetailsScreen(navController, id)
        }

        // Tenants
        composable(Screen.Tenants.route) { TenantsScreen(navController) }
        composable(Screen.AddTenant.route) { AddTenantScreen(navController) }
        composable(Screen.TenantDetails.route) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId") ?: ""
            TenantDetailsScreen(navController, tenantId)
        }


        // Other sections
        composable(Screen.Payments.route) { PaymentsScreen(navController) }
        composable(Screen.Requests.route) { RequestsScreen(navController) }
        composable(Screen.Messages.route) { MessagesScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
    }
}
