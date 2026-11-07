//package com.example.stride.navigation
//
//import androidx.compose.animation.AnimatedContentTransitionScope
//import androidx.compose.animation.core.tween
//import androidx.compose.runtime.Composable
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.stride.ui.screens.SettingsScreen
//
//@Composable
//fun MainNavigation() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "main"
//    ) {
//        composable("main") {
//            Navigation(navController = navController)
//        }
//        composable(
//            route = "settings",
//            exitTransition = {
//                slideOutOfContainer(
//                    AnimatedContentTransitionScope.SlideDirection.Left,
//                    tween(300)
//                )
//            },
//            popEnterTransition = {
//                slideIntoContainer(
//                    AnimatedContentTransitionScope.SlideDirection.Right,
//                    tween(300)
//                )
//            }
//        ) {
//            SettingsScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//    }
//}