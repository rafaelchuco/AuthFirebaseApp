package com.tecsup.authfirebaseapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.authfirebaseapp.presentation.screen.LoginScreen
import com.tecsup.authfirebaseapp.presentation.screen.RegisterScreen
import com.tecsup.authfirebaseapp.presentation.screen.HomeScreen

object Destinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun AuthApp() {
    val navController = rememberNavController()
    AuthNavGraph(navController)
}

@Composable
fun AuthNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.LOGIN
    ) {

        // ------------------ LOGIN ------------------
        composable(Destinations.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Destinations.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ------------------ REGISTER ------------------
        composable(Destinations.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ------------------ HOME ------------------
        composable(Destinations.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
