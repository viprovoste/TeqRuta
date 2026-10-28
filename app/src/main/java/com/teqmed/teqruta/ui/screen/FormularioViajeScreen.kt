package com.teqmed.teqruta.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.teqmed.teqruta.model.EstadoAuditoria
import com.teqmed.teqruta.ui.components.CampoValidado
import com.teqmed.teqruta.ui.theme.TeqNavyDark
import com.teqmed.teqruta.ui.theme.TeqNavyPrimary
import com.teqmed.teqruta.ui.theme.TeqSuccessGreen
import com.teqmed.teqruta.ui.theme.TeqWarningOrange
import com.teqmed.teqruta.viewmodel.TeqRutaViewModel
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pantalla 3: Formulario de Viaje al apretar "Inicio" o "Finalizar" (Proceso P03: R.11 a R.15, R.20).
 * - Al apretar INICIO: Selecciona destino activo, vehículo, odómetro inicial numérico y captura foto de respaldo + GPS.
 * - Al apretar FINALIZAR: Ingresa odómetro final, peajes pagados, foto de respaldo, calcula costo en tiempo real y
 *   exige justificación si el kilometraje supera en >15% la distancia oficial del tramo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioViajeScreen(
    navController: NavController,
    viewModel: TeqRutaViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val viajeState by viewModel.viajeState.collectAsState()
    val destinos by viewModel.destinos.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val traslados by viewModel.traslados.collectAsState()

    val viajeEnCurso = traslados.find { it.estadoAuditoria == EstadoAuditoria.EN_RUTA }
    val esModoCierre = viajeEnCurso != null

    var menuDestinoExpandido by remember { mutableStateOf(false) }
    var menuVehiculoExpandido by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var mostrarDialogoPermisoCamara by remember { mutableStateOf(false) }

    // Recurso Nativo 2: Cámara y Galería (Patrón exacto de MainActivity.kt en camara_geolocalizacion)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onViajeFotoCapturada(uri.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.onViajeFotoCapturada(tempCameraUri.toString())
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = crearArchivoFotoTemporal(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            mostrarDialogoPermisoCamara = true
        }
    }

    fun abrirCamaraNativa() {
        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (check == PackageManager.PERMISSION_GRANTED) {
            val uri = crearArchivoFotoTemporal(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @SuppressLint("MissingPermission")
    fun refrescarGpsEnFormulario() {
        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (check == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val lat = String.format(Locale.US, "%.4f", loc.latitude)
                    val lon = String.format(Locale.US, "%.4f", loc.longitude)
                    viewModel.onUbicacionGpsActualizada("Lat: $lat, Lon: $lon (GPS Terreno)")
                }
            }
        }
    }

    val destinoSeleccionado = destinos.find { it.id == viajeState.destinoId }
    val vehiculoSeleccionado = vehiculos.find { it.patente == viajeState.vehiculoPatente }

    // Cálculo en tiempo real para previsualización en modo Cierre
    val odoIniVal = viajeEnCurso?.odometroInicial ?: (viajeState.odometroInicial.toIntOrNull() ?: 0)
    val odoFinVal = viajeState.odometroFinal.toIntOrNull() ?: 0
    val kmCalculados = if (esModoCierre && odoFinVal > odoIniVal) odoFinVal - odoIniVal else 0
    val distOficial = viajeEnCurso?.distanciaOficialKm ?: (destinoSeleccionado?.distanciaOficialKm ?: 1.0)
    val desvPorcentaje = if (kmCalculados > 0 && distOficial > 0) {
        (((kmCalculados - distOficial) / distOficial) * 1000.0).roundToInt() / 10.0
    } else 0.0
    val hayDesvio = desvPorcentaje > 15.0

    val colorTarjetaCalculo by animateColorAsState(
        targetValue = if (hayDesvio) Color(0xFFFDEDEC) else Color(0xFFE8F8F5),
        animationSpec = tween(durationMillis = 400),
        label = "color_alerta_desvio"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (esModoCierre) "Finalizar Visita en Terreno" else "Iniciar Nuevo Viaje",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Mapa",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TeqNavyDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!esModoCierre) {
                // ============================================================
                // PASO A: APERTURA DEL TRASLADO (AL APRETAR INICIO)
                // ============================================================
                Text(
                    text = "1. Seleccione Destino, Vehículo y Odómetro Inicial",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeqNavyDark
                )

                // Selector de Destino Activo (Clínica / Hospital)
                ExposedDropdownMenuBox(
                    expanded = menuDestinoExpandido,
                    onExpandedChange = { menuDestinoExpandido = !menuDestinoExpandido }
                ) {
                    OutlinedTextField(
                        value = destinoSeleccionado?.let {
                            "${it.nombre} (${it.ciudad} • ${it.distanciaOficialKm} km)"
                        } ?: "Seleccione un destino activo...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Destino Activo (Clínica / Hospital)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuDestinoExpandido) },
                        isError = viajeState.errores.destinoId != null,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuDestinoExpandido,
                        onDismissRequest = { menuDestinoExpandido = false }
                    ) {
                        destinos.forEach { d ->
                            DropdownMenuItem(
                                text = {
                                    Text("${d.nombre} - ${d.ciudad} (Dist. Oficial: ${d.distanciaOficialKm} km)")
                                },
                                onClick = {
                                    viewModel.onViajeDestinoChange(d.id)
                                    menuDestinoExpandido = false
                                }
                            )
                        }
                    }
                }

                // Selector de Vehículo Asignado
                ExposedDropdownMenuBox(
                    expanded = menuVehiculoExpandido,
                    onExpandedChange = { menuVehiculoExpandido = !menuVehiculoExpandido }
                ) {
                    OutlinedTextField(
                        value = vehiculoSeleccionado?.let {
                            "${it.patente} • ${it.marcaModelo} (${it.tipo} - ${it.rendimientoKmL} km/L)"
                        } ?: "Seleccione vehículo...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehículo Utilizado") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuVehiculoExpandido) },
                        isError = viajeState.errores.vehiculoPatente != null,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuVehiculoExpandido,
                        onDismissRequest = { menuVehiculoExpandido = false }
                    ) {
                        vehiculos.forEach { v ->
                            DropdownMenuItem(
                                text = {
                                    Text("${v.patente} - ${v.marcaModelo} (${v.tipo} • ${v.rendimientoKmL} km/L)")
                                },
                                onClick = {
                                    viewModel.onViajeVehiculoChange(v.patente)
                                    menuVehiculoExpandido = false
                                }
                            )
                        }
                    }
                }

                // Campo Odómetro Inicial Numérico
                CampoValidado(
                    valor = viajeState.odometroInicial,
                    onValorChange = viewModel::onViajeOdometroInicialChange,
                    etiqueta = "Odómetro Inicial Numérico (km en tablero)",
                    icono = Icons.Default.Speed,
                    error = viajeState.errores.odometroInicial,
                    tipoTeclado = KeyboardType.Number
                )
            } else {
                // ============================================================
                // PASO B: CIERRE DEL TRASLADO (AL APRETAR FINALIZAR)
                // ============================================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Viaje en Curso: ${viajeEnCurso?.destinoNombre}",
                            fontWeight = FontWeight.Bold,
                            color = TeqNavyDark
                        )
                        Text(
                            text = "• Vehículo: ${viajeEnCurso?.vehiculoPatente} (${viajeEnCurso?.vehiculoTipo})\n" +
                                    "• Odómetro Inicial: ${viajeEnCurso?.odometroInicial} km\n" +
                                    "• Distancia Oficial del Tramo: ${viajeEnCurso?.distanciaOficialKm} km",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Campo Odómetro Final Numérico
                CampoValidado(
                    valor = viajeState.odometroFinal,
                    onValorChange = viewModel::onViajeOdometroFinalChange,
                    etiqueta = "Odómetro Final Numérico (km al llegar)",
                    icono = Icons.Default.Speed,
                    error = viajeState.errores.odometroFinal,
                    tipoTeclado = KeyboardType.Number
                )

                // Campo Peajes Pagados en CLP
                CampoValidado(
                    valor = viajeState.peajesClp,
                    onValorChange = viewModel::onViajePeajesChange,
                    etiqueta = "Total Peajes Pagados ($ CLP)",
                    icono = Icons.Default.AttachMoney,
                    error = viajeState.errores.peajesClp,
                    tipoTeclado = KeyboardType.Number
                )

                // Tarjeta animada con cálculo automático de kilómetros, desvío y costo (R.15, R.20, IE 2.2.2)
                AnimatedVisibility(visible = kmCalculados > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colorTarjetaCalculo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hayDesvio) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (hayDesvio) TeqWarningOrange else TeqSuccessGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (hayDesvio) {
                                        "ALERTA DE DESVIACIÓN DETECTADA (+$desvPorcentaje%)"
                                    } else {
                                        "Distancia Dentro del Rango Oficial ($desvPorcentaje%)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = if (hayDesvio) TeqWarningOrange else TeqSuccessGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Km Declarados: $kmCalculados km (Oficial: $distOficial km)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Campo Justificación (Obligatorio si hay desviación > 15%)
                CampoValidado(
                    valor = viajeState.justificacion,
                    onValorChange = viewModel::onViajeJustificacionChange,
                    etiqueta = if (hayDesvio) {
                        "Justificación Obligatoria por Desvío (>15%)"
                    } else {
                        "Observación de la Visita (Opcional)"
                    },
                    icono = Icons.AutoMirrored.Filled.Comment,
                    error = viajeState.errores.justificacion,
                    singleLine = false
                )
            }

            // ================================================================
            // SECCIÓN DE RECURSOS NATIVOS: CÁMARA (FOTO RESPALDO) Y GPS
            // ================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fotografía para Capturar Respaldos (Odómetro / Peaje)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TeqNavyDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Previsualización de la foto capturada (Patrón camara_geolocalizacion)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFECF0F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viajeState.fotoUri.isNotBlank()) {
                            if (viajeState.fotoUri.startsWith("content://") || viajeState.fotoUri.startsWith("file://")) {
                                AsyncImage(
                                    model = Uri.parse(viajeState.fotoUri),
                                    contentDescription = "Foto de respaldo capturada",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = TeqSuccessGreen,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Evidencia adjunta: ${viajeState.fotoUri}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Sin fotografía capturada aún",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = viajeState.errores.fotoRespaldo != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = viajeState.errores.fotoRespaldo ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { abrirCamaraNativa() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cámara")
                        }
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galería")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Coordenada GPS estampada
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TeqNavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viajeState.gpsActual,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { refrescarGpsEnFormulario() }) {
                            Text("Refrescar GPS")
                        }
                    }
                }
            }

            // Botón Principal de Confirmación
            Button(
                onClick = {
                    val exito = if (esModoCierre) {
                        viewModel.validarYFinalizarViaje()
                    } else {
                        viewModel.validarEIniciarViaje()
                    }
                    if (exito) {
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esModoCierre) TeqSuccessGreen else TeqNavyPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (esModoCierre) "CERRAR VISITA Y CALCULAR COSTO" else "CONFIRMAR INICIO DE VIAJE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Diálogo cuando el permiso de Cámara es denegado (Patrón camara_geolocalizacion)
    if (mostrarDialogoPermisoCamara) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisoCamara = false },
            title = { Text("Permiso de Cámara Requerido") },
            text = {
                Text("Para capturar fotografías de respaldo del odómetro o peajes es necesario otorgar el permiso de Cámara. ¿Deseas solicitarlo nuevamente?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoPermisoCamara = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Solicitar Permiso")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermisoCamara = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun crearArchivoFotoTemporal(context: Context): Uri {
    val tempFile = File.createTempFile("respaldo_teqruta_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
