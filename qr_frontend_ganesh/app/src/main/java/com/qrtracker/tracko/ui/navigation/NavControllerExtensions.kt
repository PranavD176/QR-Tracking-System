package com.qrtracker.tracko.ui.navigation

import androidx.navigation.NavController

fun NavController.navigateWithState(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
    }
}
