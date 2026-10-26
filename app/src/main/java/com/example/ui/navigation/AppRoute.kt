package com.example.ui.navigation

sealed class AppRoute(val route: String) {
    object Login : AppRoute("login")
    object Dashboard : AppRoute("dashboard")
    object DailyEntry : AppRoute("daily_entry")
    object Expenditures : AppRoute("expenditures")
    object Reports : AppRoute("reports")

    companion object {
        fun fromRoute(route: String?): AppRoute {
            return when (route) {
                Login.route -> Login
                Dashboard.route -> Dashboard
                DailyEntry.route -> DailyEntry
                Expenditures.route -> Expenditures
                Reports.route -> Reports
                else -> Dashboard
            }
        }
    }
}
