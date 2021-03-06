package com.luisma.cryptocurrency.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.luisma.cryptocurrency.domain.app.repositories.NavigationRepo
import com.luisma.cryptocurrency.ui.views.cryptoDetails.CryptoDetails
import com.luisma.cryptocurrency.ui.views.home.Home
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun Router(
    navigationManager: NavigationRepo,
) {
    val navController = rememberNavController()
    val corutineNavigation = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {

        corutineNavigation.launch {
            navigationManager.commands.collectLatest {
                navController.navigate(it.route)
            }
        }

        composable(
            Routes.Home.route
        ) {
            Home()
        }

        composable(
            Routes.CryptoDetails.ROUTE,
            arguments = Routes.CryptoDetails.ARGUMENTS
        ) {
            CryptoDetails()
        }
    }
}