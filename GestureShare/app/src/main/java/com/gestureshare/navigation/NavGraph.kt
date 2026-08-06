package com.gestureshare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gestureshare.feature.main.MainScreen

object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val PERMISSIONS = "permissions"
    const val TRANSFER_HISTORY = "history"
}

@Composable
fun GestureShareNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen()
        }
    }
}
