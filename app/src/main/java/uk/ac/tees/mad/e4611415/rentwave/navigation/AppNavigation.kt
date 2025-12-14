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
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments.LandlordPaymentDetailsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments.TenantPaymentScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests.RequestsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests.LandlordRequestDetailsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.messages.MessagesScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings.SettingsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings.AboutScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.rent.MyRentScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments.TenantPaymentsHomeScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.payments.PaymentHistoryScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests.TenantRequestsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests.CreateRequestScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.requests.TenantRequestDetailsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.messages.TenantMessagesScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.profile.TenantProfileScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings.TenantSettingsScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings.EditProfileScreen
import uk.ac.tees.mad.e4611415.rentwave.ui.screens.settings.ChangePasswordScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Authentication Screens
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.SignUp.route) { SignUpScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.Terms.route) { TermsScreen(navController) }

        // Dashboards
        composable(Screen.LandlordDashboard.route) { LandlordDashboardScreen(navController) }
        composable(
            route = Screen.ViewProfileImage.route + "/{imageUrl}",
            arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            ViewProfileImageScreen(navController, imageUrl)
        }

        composable(Screen.TenantDashboard.route) { TenantDashboardScreen(navController) }

        // Properties
        composable(Screen.Properties.route) { PropertiesScreen(navController) }
        composable(Screen.AddProperty.route) { AddPropertyScreen(navController) }
        composable(
            route = Screen.EditProperty.route + "/{propertyId}",
            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("propertyId") ?: ""
            EditPropertyScreen(navController, id)
        }

        composable(
            route = Screen.PropertyDetails.route + "/{propertyId}",
            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("propertyId") ?: ""
            PropertyDetailsScreen(navController, id)
        }

        // Tenants
        composable(Screen.Tenants.route) { TenantsScreen(navController) }
        composable(Screen.AddTenant.route) { AddTenantScreen(navController) }
        composable(
            route = Screen.TenantDetails.route + "/{tenantId}",
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId") ?: ""
            TenantDetailsScreen(navController, tenantId)
        }

        // Other Sections
        composable(Screen.Payments.route) { PaymentsScreen(navController) }
        composable(
            route = Screen.LandlordPaymentDetails.route + "/{paymentId}",
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
        ) {
            LandlordPaymentDetailsScreen(
                navController,
                it.arguments?.getString("paymentId")!!
            )
        }

        composable(Screen.Requests.route) { RequestsScreen(navController) }

        // Landlord request details
        composable(
            route = Screen.LandlordRequestDetails.route + "/{id}"
        ) {
            LandlordRequestDetailsScreen(
                navController,
                it.arguments?.getString("id")!!
            )
        }
        composable(Screen.Messages.route) { MessagesScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.About.route) {
            AboutScreen(navController)
        }


        // Tenant Rent Screen 🚀
        composable(Screen.MyRent.route) { MyRentScreen(navController) }
        composable(Screen.TenantPayment.route) { TenantPaymentScreen(navController) }
        composable(Screen.TenantPaymentsHome.route) {
            TenantPaymentsHomeScreen(navController)
        }
        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(navController)
        }
        composable(Screen.TenantRequestsHome.route) {
            TenantRequestsScreen(navController)
        }

        composable(Screen.TenantCreateRequest.route) {
            CreateRequestScreen(navController)
        }

        composable(
            route = Screen.TenantRequestDetails.route + "/{requestId}",
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("requestId") ?: ""
            TenantRequestDetailsScreen(navController, id)
        }
        composable(Screen.TenantMessages.route) {
            TenantMessagesScreen(navController)
        }

        composable(Screen.TenantProfile.route) { TenantProfileScreen(navController) }

        composable(Screen.TenantSettings.route) { TenantSettingsScreen(navController) }
        composable(Screen.EditProfile.route) { EditProfileScreen(navController) }
        composable(Screen.ChangePassword.route) { ChangePasswordScreen(navController) }






    }
}
