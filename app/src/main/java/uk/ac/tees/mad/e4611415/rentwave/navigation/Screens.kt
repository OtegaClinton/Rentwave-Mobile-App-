package uk.ac.tees.mad.e4611415.rentwave.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object Terms : Screen("terms_screen")
    object LandlordDashboard : Screen("landlord_dashboard")
    object TenantDashboard : Screen("tenant_dashboard")
    object Properties : Screen("properties_screen")
    object AddProperty : Screen("add_property_screen")
    object EditProperty : Screen("edit_property_screen/{propertyId}") {
        fun passId(id: String) = "edit_property_screen/$id"
    }
    object PropertyDetails : Screen("property_details_screen/{propertyId}") {
        fun passId(id: String) = "property_details_screen/$id"
    }

    object Tenants : Screen("tenants_screen")
    object AddTenant : Screen("add_tenant_screen")
    object TenantDetails : Screen("tenant_details_screen/{tenantId}") {
        fun passId(id: String) = "tenant_details_screen/$id"
    }

    object Payments : Screen("payments_screen")
    object Requests : Screen("requests_screen")
    object Messages : Screen("messages_screen")
    object Settings : Screen("settings_screen")
}
