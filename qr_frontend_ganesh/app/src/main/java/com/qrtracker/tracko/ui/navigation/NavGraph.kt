package com.qrtracker.tracko.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qrtracker.tracko.ui.auth.LoginScreen
import com.qrtracker.tracko.ui.auth.UserProfileScreen
import com.qrtracker.tracko.ui.auth.RegisterScreen
import com.qrtracker.tracko.ui.alerts.AdminAlertScreen
import com.qrtracker.tracko.ui.alerts.AlertFeedScreen
import com.qrtracker.tracko.ui.alerts.AppAlertsScreen
import com.qrtracker.tracko.ui.admin.AdminProfileScreen
import com.qrtracker.tracko.ui.checkpoint.AdminCheckpointScreen
import com.qrtracker.tracko.ui.packages.AdminPackagesScreen
import com.qrtracker.tracko.ui.packages.CreatePackageScreen
import com.qrtracker.tracko.ui.packages.PackageDetailScreen
import com.qrtracker.tracko.ui.packages.PackageListScreen
import com.qrtracker.tracko.ui.scan.ScanResultScreen
import com.qrtracker.tracko.ui.scan.ScanScreen
import com.qrtracker.tracko.utils.TokenManager

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier
    ) {

        // ── Auth ─────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }

        composable(Routes.USER_PROFILE) {
            UserProfileScreen(navController = navController)
        }

        // ── Home (Active Pulse homepage) ─────────────────────────────────────
        composable(Routes.HOME) {
            PackageListScreen(navController = navController, startWithAll = false)
        }

        // ── Packages (Full list) ─────────────────────────────────────────────
        composable(Routes.PACKAGE_LIST) {
            PackageListScreen(navController = navController, startWithAll = true)
        }

        composable(
            route = Routes.PACKAGE_DETAIL,
            arguments = listOf(
                navArgument("packageId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val packageId = backStackEntry.arguments?.getString("packageId") ?: ""
            PackageDetailScreen(
                navController = navController,
                packageId     = packageId
            )
        }

        composable(Routes.CREATE_PACKAGE) {
            CreatePackageScreen(navController = navController)
        }

        composable(Routes.ADMIN_CREATE_PACKAGE) {
            CreatePackageScreen(navController = navController, isAdminFlow = true)
        }

        // ── Scanner ───────────────────────────────────────────────────────────
        composable(Routes.SCANNER) {
            ScanScreen(navController = navController)
        }

        composable(
            route = Routes.SCAN_RESULT,
            arguments = listOf(
                navArgument("result")      { type = NavType.StringType  },
                navArgument("packageDesc") { type = NavType.StringType  },
                navArgument("ownerName")   { type = NavType.StringType  },
                navArgument("alertSent")   { type = NavType.BoolType    }
            )
        ) { backStackEntry ->
            val result      = backStackEntry.arguments?.getString("result")      ?: ""
            val packageDesc = backStackEntry.arguments?.getString("packageDesc") ?: ""
            val ownerName   = backStackEntry.arguments?.getString("ownerName")   ?: ""
            val alertSent   = backStackEntry.arguments?.getBoolean("alertSent")  ?: false

            ScanResultScreen(
                navController = navController,
                result        = result,
                packageDesc   = packageDesc,
                ownerName     = ownerName,
                alertSent     = alertSent
            )
        }

        // ── User Alerts (bottom nav for users) ────────────────────────────────
        composable(Routes.ALERTS) {
            AlertFeedScreen(navController = navController)
        }

        // ── Admin Alerts (bottom nav for admin) ───────────────────────────────
        composable(Routes.ADMIN_ALERTS) {
            AdminAlertScreen(navController = navController)
        }

        // ── App Alerts (system notifications, from bell icon) ─────────────────
        composable(Routes.APP_ALERTS) {
            AppAlertsScreen(navController = navController)
        }

        // ── Admin Checkpoint (main landing for admin) ─────────────────────────
        composable(Routes.ADMIN_CHECKPOINT) {
            AdminCheckpointScreen(navController = navController)
        }

        composable(Routes.ADMIN_PACKAGES) {
            AdminPackagesScreen(navController = navController)
        }

        composable(Routes.ADMIN_PROFILE) {
            AdminProfileScreen(navController = navController)
        }
    }
}

