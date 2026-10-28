package com.teqmed.teqruta.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.teqmed.teqruta.model.EstadoAuditoria
import com.teqmed.teqruta.ui.components.MapaRadarGps
import com.teqmed.teqruta.ui.components.ModalConfiguracionTuerca
import com.teqmed.teqruta.ui.theme.TeqNavyDark
import com.teqmed.teqruta.ui.theme.TeqNavyPrimary
import com.teqmed.teqruta.ui.theme.TeqSuccessGreen
import com.teqmed.teqruta.ui.theme.TeqWarningOrange
import com.teqmed.teqruta.viewmodel.TeqRutaViewModel
import java.util.Locale

/**
 * Pantalla 2: Pantalla Principal del Conductor (Proceso P03: R.11 a R.19).
 * Diseño:
 * - Mapa central que marca dónde se encuentra la persona (GPS Nativo).
 * - Abajo un botón principal que dice "INICIO" (o "FINALIZAR VIAJE" si va en ruta).
 * - Al lado del botón, una tuerca (⚙️) para abrir Configuración y Cerrar Sesión.
 */
@Composable
fun MapaConductorScreen(
    navController: NavController,
    viewModel: TeqRutaViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val usuario by viewModel.usuarioActual.collectAsState()
    val traslados by viewModel.traslados.collectAsState()
    val tarifas by viewModel.tarifas.collectAsState()
    val viajeState by viewModel.viajeState.collectAsState()
    val mensajeAnimado by viewModel.mensajeExitoAnimado.collectAsState()
    val sincronizando by viewModel.sincronizandoOffline.collectAsState()

    var mostrarModalTuerca by remember { mutableStateOf(false) }
    var mostrarDialogoPermisoGps by remember { mutableStateOf(false) }

    val viajeEnCurso = traslados.find { it.estadoAuditoria == EstadoAuditoria.EN_RUTA }
    val estaEnRuta = viajeEnCurso != null
    val pendientesSync = traslados.count { !it.sincronizado && it.estadoAuditoria != EstadoAuditoria.EN_RUTA }

    // Animaciones de estado (Patrón exacto de PantallaPrincipal.kt en AnimacionesEstados)
    val colorBotonPrincipal by animateColorAsState(
        targetValue = if (estaEnRuta) TeqWarningOrange else TeqNavyPrimary,
        animationSpec = tween(durationMillis = 500),
        label = "color_boton_viaje"
    )

    val textoBotonPrincipal by remember(estaEnRuta) {
        derivedStateOf {
            if (estaEnRuta) "FINALIZAR VIAJE EN CURSO" else "INICIO (INICIAR VIAJE)"
        }
    }

    // Recurso Nativo 1: Geolocalización GPS (Patrón de LocationActivity.kt en camara_geolocalizacion)
    @SuppressLint("MissingPermission")
    fun obtenerCoordenadasDispositivo() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = String.format(Locale.US, "%.4f", location.latitude)
                    val lon = String.format(Locale.US, "%.4f", location.longitude)
                    viewModel.onUbicacionGpsActualizada("Lat: $lat, Lon: $lon (GPS Dispositivo)")
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            val lat = String.format(Locale.US, "%.4f", lastLoc.latitude)
                            val lon = String.format(Locale.US, "%.4f", lastLoc.longitude)
                            viewModel.onUbicacionGpsActualizada("Lat: $lat, Lon: $lon (GPS Activo)")
                        } else {
                            viewModel.onUbicacionGpsActualizada("Lat: -36.8269, Lon: -73.0497 (Sede Concepción)")
                        }
                    }
                }
            }
            .addOnFailureListener {
                viewModel.onUbicacionGpsActualizada("Lat: -36.8269, Lon: -73.0497 (Sede Concepción)")
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            obtenerCoordenadasDispositivo()
        } else {
            mostrarDialogoPermisoGps = true
        }
    }

    fun verificarYSolicitarUbicacion() {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
            obtenerCoordenadasDispositivo()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineCheck == PackageManager.PERMISSION_GRANTED) {
            obtenerCoordenadasDispositivo()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Barra Superior Informativa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TeqRuta – Terreno",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TeqNavyDark
                )
                Text(
                    text = "Conductor: ${usuario?.nombre ?: "Juan Pérez"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            Surface(
                color = if (pendientesSync > 0) Color(0xFFFFF3E0) else Color(0xFFE8F8F5),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (pendientesSync > 0) "Offline ($pendientesSync por sync)" else "Sincronizado OK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (pendientesSync > 0) TeqWarningOrange else TeqSuccessGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Banner animado de confirmación cuando se guarda o sincroniza un viaje (IE 2.2.2)
        AnimatedVisibility(visible = mensajeAnimado != null) {
            Surface(
                color = Color(0xFFE8F8F5),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = TeqSuccessGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mensajeAnimado ?: "",
                        color = TeqSuccessGreen,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // MAPA PRINCIPAL QUE MARCA DÓNDE SE ENCUENTRA LA PERSONA
        MapaRadarGps(
            coordenadasGps = viajeState.gpsActual,
            viajeEnCurso = viajeEnCurso,
            onActualizarGpsClick = { verificarYSolicitarUbicacion() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Historial rápido de últimos viajes (permite tocar para abrir la Ficha Completa paso a paso)
        Text(
            text = "Mis Últimos Traslados Registrados (Toque para ver Ficha):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = TeqNavyDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(traslados) { tr ->
                Card(
                    modifier = Modifier
                        .width(235.dp)
                        .clickable { navController.navigate("ficha_detalle/${tr.id}") },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (tr.tieneDesviacion) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (tr.tieneDesviacion) TeqWarningOrange else TeqSuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr.destinoNombre,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = if (tr.estadoAuditoria == EstadoAuditoria.EN_RUTA) {
                                "En Ruta • Odo Ini: ${tr.odometroInicial} km"
                            } else {
                                "${tr.kmDeclarados} km • Costo: $${tr.costoTotalClp} CLP"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BARRA INFERIOR: BOTÓN "INICIO" + TUERCA DE CONFIGURACIÓN (⚙️) AL LADO
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    viewModel.prepararFormularioViaje()
                    navController.navigate("formulario_viaje")
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorBotonPrincipal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
            ) {
                Icon(
                    imageVector = if (estaEnRuta) Icons.Default.Flag else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = textoBotonPrincipal,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botón de Tuerca ⚙️ al lado para Configuración y Cerrar Sesión
            FilledTonalIconButton(
                onClick = { mostrarModalTuerca = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración y Cerrar Sesión",
                    tint = TeqNavyDark,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Modal de Configuración (Tuerca ⚙️)
    if (mostrarModalTuerca) {
        ModalConfiguracionTuerca(
            usuario = usuario,
            tarifas = tarifas,
            pendientesSync = pendientesSync,
            sincronizando = sincronizando,
            onSincronizarClick = { viewModel.sincronizarViajesOffline() },
            onCerrarSesionClick = {
                mostrarModalTuerca = false
                viewModel.cerrarSesion()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            onDismiss = { mostrarModalTuerca = false }
        )
    }

    // Diálogo seguro cuando el permiso de Ubicación GPS es denegado (IE 2.4.1)
    if (mostrarDialogoPermisoGps) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisoGps = false },
            title = { Text("Permiso de Ubicación Requerido") },
            text = {
                Text("Para marcar dónde te encuentras en el mapa y respaldar el traslado es necesario otorgar el permiso de geolocalización GPS.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoPermisoGps = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Solicitar Permiso")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermisoGps = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
