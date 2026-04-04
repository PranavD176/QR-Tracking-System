package com.ganesh.qrtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ganesh.qrtracker.ui.auth.LoginScreen
import com.ganesh.qrtracker.ui.auth.RegisterScreen
import com.ganesh.qrtracker.ui.packages.CreatePackageScreen
import com.ganesh.qrtracker.ui.packages.PackageDetailScreen
import com.ganesh.qrtracker.ui.packages.PackageListScreen
import com.ganesh.qrtracker.ui.scan.ScanResultScreen
import com.ganesh.qrtracker.ui.scan.ScanScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN,
    modifier: Modifier = Modifier
) {
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

        // ── Packages ─────────────────────────────────────────────────────────
        composable(Routes.PACKAGE_LIST) {
            PackageListScreen(navController = navController)
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

        // ── Alerts (Member 2) ─────────────────────────────────────────────────
        // composable(Routes.ALERTS) {
        //     AlertFeedScreen(navController = navController)
        // }

        // ── Admin Alerts (Member 2) ───────────────────────────────────────────
        // composable(Routes.ADMIN_ALERTS) {
        //     AdminAlertsScreen(navController = navController)
        // }
    }
}