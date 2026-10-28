package com.teqmed.teqruta.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teqmed.teqruta.model.EstadoAuditoria
import com.teqmed.teqruta.ui.components.CampoValidado
import com.teqmed.teqruta.ui.components.ModalConfiguracionTuerca
import com.teqmed.teqruta.ui.theme.TeqNavyDark
import com.teqmed.teqruta.ui.theme.TeqSuccessGreen
import com.teqmed.teqruta.viewmodel.TeqRutaViewModel

/**
 * Pantalla 4: Panel del Administrador (Proceso P02: R.06 a R.10).
 * Incluye los 3 módulos con botones y formularios validados:
 * 1. Agregar vehículos de los trabajadores.
 * 2. Guardar destinos (clínicas/hospitales con distancia oficial en km).
 * 3. Actualizar el precio por litro de combustible (fuente CNE).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navController: NavController,
    viewModel: TeqRutaViewModel
) {
    val usuario by viewModel.usuarioActual.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val destinos by viewModel.destinos.collectAsState()
    val tarifas by viewModel.tarifas.collectAsState()
    val traslados by viewModel.traslados.collectAsState()
    val mensajeAnimado by viewModel.mensajeExitoAnimado.collectAsState()
    val sincronizando by viewModel.sincronizandoOffline.collectAsState()

    val vehiculoForm by viewModel.vehiculoFormState.collectAsState()
    val destinoForm by viewModel.destinoFormState.collectAsState()
    val tarifaForm by viewModel.tarifaFormState.collectAsState()

    var tabSeleccionada by remember { mutableIntStateOf(0) }
    var mostrarModalTuerca by remember { mutableStateOf(false) }
    val pendientesSync = traslados.count { !it.sincronizado && it.estadoAuditoria != EstadoAuditoria.EN_RUTA }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Panel Administrador",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catálogos y Tarifas Centralizadas (TEQMED SpA)",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarModalTuerca = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración y Cerrar Sesión",
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
        ) {
            TabRow(selectedTabIndex = tabSeleccionada) {
                Tab(
                    selected = tabSeleccionada == 0,
                    onClick = { tabSeleccionada = 0 },
                    text = { Text("🚗 Vehículos") }
                )
                Tab(
                    selected = tabSeleccionada == 1,
                    onClick = { tabSeleccionada = 1 },
                    text = { Text("🏥 Destinos") }
                )
                Tab(
                    selected = tabSeleccionada == 2,
                    onClick = { tabSeleccionada = 2 },
                    text = { Text("⛽ Combustible") }
                )
            }

            // Mensaje animado de confirmación de guardado (IE 2.2.2)
            AnimatedVisibility(visible = mensajeAnimado != null) {
                Surface(
                    color = Color(0xFFE8F8F5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Crossfade(
                targetState = tabSeleccionada,
                label = "transicion_tabs_admin",
                modifier = Modifier.fillMaxSize()
            ) { indice ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (indice) {
                        0 -> {
                            // ====================================================
                            // TAB 0: AGREGAR VEHÍCULOS DE LOS TRABAJADORES (R.06)
                            // ====================================================
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Agregar Vehículo de Trabajador",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TeqNavyDark
                                    )

                                    CampoValidado(
                                        valor = vehiculoForm.patente,
                                        onValorChange = viewModel::onVehiculoPatenteChange,
                                        etiqueta = "Patente del Vehículo (ej. KXYZ-89)",
                                        icono = Icons.Default.Badge,
                                        error = vehiculoForm.errores.patente
                                    )

                                    CampoValidado(
                                        valor = vehiculoForm.marcaModelo,
                                        onValorChange = viewModel::onVehiculoMarcaChange,
                                        etiqueta = "Marca y Modelo (ej. Mitsubishi L200)",
                                        icono = Icons.Default.DirectionsCar,
                                        error = vehiculoForm.errores.marcaModelo
                                    )

                                    Text(
                                        text = "Tipo de Propiedad:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        FilterChip(
                                            selected = vehiculoForm.tipo == "Empresa",
                                            onClick = { viewModel.onVehiculoTipoChange("Empresa") },
                                            label = { Text("Vehículo de Empresa") }
                                        )
                                        FilterChip(
                                            selected = vehiculoForm.tipo == "Particular",
                                            onClick = { viewModel.onVehiculoTipoChange("Particular") },
                                            label = { Text("Vehículo Particular") }
                                        )
                                    }

                                    CampoValidado(
                                        valor = vehiculoForm.rendimientoKmL,
                                        onValorChange = viewModel::onVehiculoRendimientoChange,
                                        etiqueta = "Rendimiento Promedio (km por litro)",
                                        icono = Icons.Default.Speed,
                                        error = vehiculoForm.errores.rendimientoKmL,
                                        tipoTeclado = KeyboardType.Decimal
                                    )

                                    CampoValidado(
                                        valor = vehiculoForm.trabajadorAsignado,
                                        onValorChange = viewModel::onVehiculoTrabajadorChange,
                                        etiqueta = "Trabajador / Técnico Asignado",
                                        icono = Icons.Default.Person,
                                        error = vehiculoForm.errores.trabajadorAsignado
                                    )

                                    Button(
                                        onClick = { viewModel.validarYGuardarVehiculo() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        Text(
                                            text = "AGREGAR VEHÍCULO DE TRABAJADOR",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Flota Registrada (${vehiculos.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TeqNavyDark
                            )
                            vehiculos.forEach { v ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${v.patente} • ${v.marcaModelo} (${v.tipo})",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Rendimiento: ${v.rendimientoKmL} km/L • Asignado a: ${v.trabajadorAsignado}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            // ====================================================
                            // TAB 1: GUARDAR DESTINOS (CLÍNICAS Y HOSPITALES) (R.07)
                            // ====================================================
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Guardar Nuevo Destino (Clínica / Hospital)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TeqNavyDark
                                    )

                                    CampoValidado(
                                        valor = destinoForm.nombre,
                                        onValorChange = viewModel::onDestinoNombreChange,
                                        etiqueta = "Nombre de Clínica, Hospital o Centro Diálisis",
                                        icono = Icons.Default.LocalHospital,
                                        error = destinoForm.errores.nombre
                                    )

                                    CampoValidado(
                                        valor = destinoForm.ciudad,
                                        onValorChange = viewModel::onDestinoCiudadChange,
                                        etiqueta = "Ciudad (Concepción, Chillán, Los Ángeles, Temuco)",
                                        icono = Icons.Default.LocationCity,
                                        error = destinoForm.errores.ciudad
                                    )

                                    CampoValidado(
                                        valor = destinoForm.direccion,
                                        onValorChange = viewModel::onDestinoDireccionChange,
                                        etiqueta = "Dirección Exacta del Cliente",
                                        icono = Icons.Default.Map,
                                        error = destinoForm.errores.direccion
                                    )

                                    CampoValidado(
                                        valor = destinoForm.distanciaOficialKm,
                                        onValorChange = viewModel::onDestinoDistanciaChange,
                                        etiqueta = "Distancia Oficial del Tramo desde Sede (km)",
                                        icono = Icons.Default.Speed,
                                        error = destinoForm.errores.distanciaOficialKm,
                                        tipoTeclado = KeyboardType.Decimal
                                    )

                                    CampoValidado(
                                        valor = destinoForm.peajeBaseClp,
                                        onValorChange = viewModel::onDestinoPeajeChange,
                                        etiqueta = "Peaje Referencial del Tramo ($ CLP)",
                                        icono = Icons.Default.AttachMoney,
                                        error = destinoForm.errores.peajeBaseClp,
                                        tipoTeclado = KeyboardType.Number
                                    )

                                    Button(
                                        onClick = { viewModel.validarYGuardarDestino() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        Text(
                                            text = "GUARDAR DESTINO EN CATASTRO",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Catastro Oficial de Destinos (${destinos.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TeqNavyDark
                            )
                            destinos.forEach { d ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${d.nombre} (${d.ciudad})",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Dirección: ${d.direccion}\nDistancia Oficial: ${d.distanciaOficialKm} km • Peaje Base: $${d.peajeBaseClp} CLP",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // ====================================================
                            // TAB 2: PRECIO POR LITRO DE COMBUSTIBLE (R.08, R.09)
                            // ====================================================
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Precio por Litro de Combustible y Tarifas",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TeqNavyDark
                                    )
                                    Text(
                                        text = "Valores centralizados basados en fuente pública de precios de combustible (CNE / ENAP Región del Biobío). Ningún técnico puede modificar esta tarifa.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )

                                    HorizontalDivider()

                                    CampoValidado(
                                        valor = tarifaForm.precioCombustibleLitro,
                                        onValorChange = viewModel::onTarifaCombustibleChange,
                                        etiqueta = "Precio por Litro de Combustible ($ CLP / Litro)",
                                        icono = Icons.Default.LocalGasStation,
                                        error = tarifaForm.errores.precioCombustibleLitro,
                                        tipoTeclado = KeyboardType.Number
                                    )

                                    CampoValidado(
                                        valor = tarifaForm.reembolsoParticularKm,
                                        onValorChange = viewModel::onTarifaReembolsoChange,
                                        etiqueta = "Tarifa Desgaste Vehículo Particular ($ CLP / km)",
                                        icono = Icons.Default.AttachMoney,
                                        error = tarifaForm.errores.reembolsoParticularKm,
                                        tipoTeclado = KeyboardType.Number
                                    )

                                    Button(
                                        onClick = { viewModel.validarYGuardarTarifas() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        Text(
                                            text = "GUARDAR PRECIO POR LITRO DE COMBUSTIBLE",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Última actualización registrada: ${tarifas.ultimaActualizacion}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TeqSuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
}
