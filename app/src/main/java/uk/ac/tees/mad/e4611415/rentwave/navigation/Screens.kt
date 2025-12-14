package uk.ac.tees.mad.e4611415.rentwave.navigation

sealed class Screen(val route: String) {

    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object Terms : Screen("terms_screen")

    object LandlordDashboard : Screen("landlord_dashboard")

    object ViewProfileImage : Screen("ViewProfileImage") {
        fun passUrl(url: String) = "ViewProfileImage/$url"
    }

    object TenantDashboard : Screen("tenant_dashboard")

    // Properties
    object Properties : Screen("properties_screen")
    object AddProperty : Screen("add_property_screen")
    object EditProperty : Screen("edit_property_screen") {
        fun passId(id: String) = "edit_property_screen/$id"
    }
    object PropertyDetails : Screen("property_details_screen") {
        fun passId(id: String) = "property_details_screen/$id"
    }

    // Tenants
    object Tenants : Screen("tenants_screen")
    object AddTenant : Screen("add_tenant_screen")
    object TenantDetails : Screen("tenant_details_screen") {
        fun passId(id: String) = "tenant_details_screen/$id"
    }

    // Other features
    object Payments : Screen("payments_screen")
    object Requests : Screen("requests_screen")
    object LandlordRequestDetails : Screen("landlord_request_details") {
        fun passId(id: String) = "landlord_request_details/$id"
    }
    object Messages : Screen("messages_screen")
    object Settings : Screen("settings_screen")
    object About : Screen("about_screen")


    // Tenant Rent
    object MyRent : Screen("my_rent_screen")
    object TenantPayment : Screen("tenant_payment_screen")

    object TenantPaymentsHome : Screen("tenant_payments_home")
    object PaymentHistory : Screen("payment_history_screen")
    // Tenant Requests
    object TenantRequestsHome : Screen("tenant_requests_home")
    object TenantCreateRequest : Screen("tenant_create_request_screen")
    object TenantRequestDetails : Screen("tenant_request_details") {
        fun passId(id: String) = "tenant_request_details/$id"
    }
    object TenantMessages : Screen("tenant_messages_screen")

    object TenantProfile : Screen("tenant_profile_screen")

    object TenantSettings : Screen("tenant_settings")
    object EditProfile : Screen("edit_profile")
    object ChangePassword : Screen("change_password")

    object LandlordPaymentDetails : Screen("landlord_payment_details") {
        fun passId(id: String) = "landlord_payment_details/$id"
    }





}
