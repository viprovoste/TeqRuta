package com.teqmed.teqruta.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teqmed.teqruta.model.RolUsuario
import com.teqmed.teqruta.ui.screen.AdminPanelScreen
import com.teqmed.teqruta.ui.screen.FichaDetalleTrasladoScreen
import com.teqmed.teqruta.ui.screen.FormularioViajeScreen
import com.teqmed.teqruta.ui.screen.JefeServicioScreen
import com.teqmed.teqruta.ui.screen.LoginRegistroScreen
import com.teqmed.teqruta.ui.screen.MapaConductorScreen
import com.teqmed.teqruta.viewmodel.TeqRutaViewModel

/**
 * Grafo de Navegación Centralizado en Jetpack Compose (Patrón de FormulariosKotlinDuoc).
 * Garantiza una navegación fluida, estructurada y consistente por rol (IE 2.1.1, R.02).
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    teqRutaViewModel: TeqRutaViewModel = viewModel()
) {
    val navController = rememberNavController()
    val cargando by teqRutaViewModel.cargandoDatos.collectAsState()
    val usuarioActual by teqRutaViewModel.usuarioActual.collectAsState()

    if (cargando) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val destinoInicial = when (usuarioActual?.rol) {
            RolUsuario.TECNICO -> "mapa_conductor"
            RolUsuario.JEFE_SERVICIO -> "jefe_servicio"
            RolUsuario.ADMIN -> "panel_admin"
            null -> "login"
        }

        NavHost(
            navController = navController,
            startDestination = destinoInicial,
            modifier = modifier
        ) {
            composable(route = "login") {
                LoginRegistroScreen(
                    navController = navController,
                    viewModel = teqRutaViewModel
                )
            }

            composable(route = "mapa_conductor") {
                MapaConductorScreen(
                    navController = navController,
                    viewModel = teqRutaViewModel
                )
            }

            composable(route = "formulario_viaje") {
                FormularioViajeScreen(
                    navController = navController,
                    viewModel = teqRutaViewModel
                )
            }

            composable(route = "panel_admin") {
                AdminPanelScreen(
                    navController = navController,
                    viewModel = teqRutaViewModel
                )
            }

            composable(route = "jefe_servicio") {
                JefeServicioScreen(
                    navController = navController,
                    viewModel = teqRutaViewModel
                )
            }

            composable(
                route = "ficha_detalle/{trasladoId}",
                arguments = listOf(navArgument("trasladoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val trasladoId = backStackEntry.arguments?.getString("trasladoId") ?: ""
                FichaDetalleTrasladoScreen(
                    trasladoId = trasladoId,
                    navController = navController,
                    viewModel = teqRutaViewModel
                )
            }
        }
    }
}
