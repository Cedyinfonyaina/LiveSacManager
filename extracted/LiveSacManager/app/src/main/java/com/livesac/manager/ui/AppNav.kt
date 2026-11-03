package com.livesac.manager.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "lives") {

        // Écran 1 : liste des Lives
        composable("lives") {
            LivesScreen(onOpenLive = { id -> nav.navigate("live/$id") })
        }

        // Écran 2 : récapitulatif d'un Live
        composable(
            route = "live/{liveId}",
            arguments = listOf(navArgument("liveId") { type = NavType.LongType })
        ) { entry ->
            val liveId = entry.arguments?.getLong("liveId") ?: 0L
            LiveScreen(
                liveId = liveId,
                onBack = { nav.popBackStack() },
                onAddJp = { nav.navigate("add/$liveId") },
                onOpenClient = { clientId -> nav.navigate("client/$clientId") }
            )
        }

        // Écran 3 : ajouter un JP
        composable(
            route = "add/{liveId}",
            arguments = listOf(navArgument("liveId") { type = NavType.LongType })
        ) { entry ->
            val liveId = entry.arguments?.getLong("liveId") ?: 0L
            AddJpScreen(liveId = liveId, onBack = { nav.popBackStack() })
        }

        // Écran 4 : détail d'un client
        composable(
            route = "client/{clientId}",
            arguments = listOf(navArgument("clientId") { type = NavType.LongType })
        ) { entry ->
            val clientId = entry.arguments?.getLong("clientId") ?: 0L
            ClientScreen(clientId = clientId, onBack = { nav.popBackStack() })
        }
    }
}
