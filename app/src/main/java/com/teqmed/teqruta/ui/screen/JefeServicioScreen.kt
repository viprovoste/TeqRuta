package com.teqmed.teqruta.ui.screen

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.teqmed.teqruta.model.EstadoAuditoria
import com.teqmed.teqruta.ui.components.CampoValidado
import com.teqmed.teqruta.ui.components.ModalConfiguracionTuerca
import com.teqmed.teqruta.ui.theme.TeqErrorRed
import com.teqmed.teqruta.ui.theme.TeqNavyDark
import com.teqmed.teqruta.ui.theme.TeqSuccessGreen
import com.teqmed.teqruta.ui.theme.TeqWarningOrange
import com.teqmed.teqruta.viewmodel.TeqRutaViewModel

/**
 * Pantalla 5: Panel del Jefe de Servicio y Finanzas (Proceso P04: R.20, R.21, R.22).
 * - Permite inspeccionar la justificación, foto y GPS de cada traslado.
 * - Permite registrar su aprobación o rechazo con comentario de auditoría validado.
 * - Incluye un botón en cada traslado para abrir su Ficha Completa con miniatura de foto,
 *   coordenadas GPS y el cálculo paso a paso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JefeServicioScreen(
    navController: NavController,
    viewModel: TeqRutaViewModel
) {
    val usuario by viewModel.usuarioActual.collectAsState()
    val traslados by viewModel.traslados.collectAsState()
    val destinos by viewModel.destinos.collectAsState()
    val tarifas by viewModel.tarifas.collectAsState()
    val filtroCliente by viewModel.filtroCliente.collectAsState()
    val comentarios by viewModel.comentariosAuditoria.collectAsState()
    val erroresAuditoria by viewModel.erroresAuditoria.collectAsState()
    val mensajeAnimado by viewModel.mensajeExitoAnimado.collectAsState()
    val sincronizando by viewModel.sincronizandoOffline.collectAsState()

    var mostrarModalTuerca by remember { mutableStateOf(false) }

    val trasladosFiltrados = traslados.filter {
        filtroCliente == "Todos" || it.destinoNombre == filtroCliente
    }
    val costoTotalFiltrado = trasladosFiltrados.sumOf { it.costoTotalClp }
    val totalDesviaciones = trasladosFiltrados.count { it.tieneDesviacion }
    val pendientesSync = traslados.count { !it.sincronizado && it.estadoAuditoria != EstadoAuditoria.EN_RUTA }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Supervisión y Auditoría",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Jefe de Servicio Técnico y Finanzas",
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
                .padding(14.dp)
        ) {
            // Tarjetas superiores de Costo Real por Contrato y Desviaciones (R.22)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "GASTO MOVILIZACIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TeqNavyDark
                        )
                        Text(
                            text = "$$costoTotalFiltrado CLP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TeqNavyDark
                        )
                        Text(
                            text = "${trasladosFiltrados.size} traslados auditados",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (totalDesviaciones > 0) Color(0xFFFFF3E0) else Color(0xFFE8F8F5)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DESVIACIONES (>15%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (totalDesviaciones > 0) TeqWarningOrange else TeqSuccessGreen
                        )
                        Text(
                            text = "$totalDesviaciones casos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalDesviaciones > 0) TeqWarningOrange else TeqSuccessGreen
                        )
                        Text(
                            text = "Tolerancia máx. +15%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filtro por Cliente / Clínica para evaluar rentabilidad de cada contrato (R.22)
            Text(
                text = "Filtrar Costo por Cliente (Contrato):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TeqNavyDark
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = filtroCliente == "Todos",
                        onClick = { viewModel.onFiltroClienteChange("Todos") },
                        label = { Text("Todos los Clientes") }
                    )
                }
                items(destinos) { d ->
                    FilterChip(
                        selected = filtroCliente == d.nombre,
                        onClick = { viewModel.onFiltroClienteChange(d.nombre) },
                        label = { Text(d.nombre) }
                    )
                }
            }

            // Mensaje animado de confirmación al aprobar o rechazar un traslado (IE 2.2.2)
            AnimatedVisibility(visible = mensajeAnimado != null) {
                Surface(
                    color = Color(0xFFE8F8F5),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
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
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Listado de traslados del equipo técnico para inspección y auditoría (R.21)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(trasladosFiltrados, key = { it.id }) { tr ->
                    val comentarioActual = comentarios[tr.id] ?: tr.comentarioAuditoria
                    val errorComentario = erroresAuditoria[tr.id]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Cabecera del traslado y estado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (tr.tieneDesviacion) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (tr.tieneDesviacion) TeqWarningOrange else TeqSuccessGreen
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "${tr.id} • ${tr.destinoNombre}",
                                            fontWeight = FontWeight.Bold,
                                            color = TeqNavyDark
                                        )
                                        Text(
                                            text = "Técnico: ${tr.tecnicoNombre} • Vehículo: ${tr.vehiculoPatente}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                                Surface(
                                    color = when (tr.estadoAuditoria) {
                                        EstadoAuditoria.CON_DESVIACION -> Color(0xFFFFF3E0)
                                        EstadoAuditoria.RECHAZADO -> Color(0xFFFDEDEC)
                                        else -> Color(0xFFE8F8F5)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = tr.estadoAuditoria.etiqueta,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (tr.estadoAuditoria) {
                                            EstadoAuditoria.CON_DESVIACION -> TeqWarningOrange
                                            EstadoAuditoria.RECHAZADO -> TeqErrorRed
                                            else -> TeqSuccessGreen
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider()

                            // Inspección de Foto en miniatura, GPS y Justificación del Técnico
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFECF0F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tr.fotoUri.startsWith("content://") || tr.fotoUri.startsWith("file://")) {
                                        AsyncImage(
                                            model = Uri.parse(tr.fotoUri),
                                            contentDescription = "Miniatura foto evidencia",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoCamera,
                                                contentDescription = null,
                                                tint = TeqNavyDark
                                            )
                                            Text(
                                                text = "Foto OK",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Declarado: ${tr.kmDeclarados} km vs. Oficial: ${tr.distanciaOficialKm} km (${if (tr.porcentajeDesviacion >= 0) "+" else ""}${tr.porcentajeDesviacion}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tr.tieneDesviacion) TeqWarningOrange else TeqNavyDark
                                    )
                                    Text(
                                        text = "Costo Calculado: $${tr.costoTotalClp} CLP (Peajes: $${tr.peajesClp})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = TeqNavyDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = tr.gpsFin.ifBlank { tr.gpsInicio },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Justificación Técnico: \"${tr.justificacionTecnico.ifBlank { "En ruta actualmente" }}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )

                            // Campo de Comentario de Auditoría del Jefe de Servicio (Validado)
                            CampoValidado(
                                valor = comentarioActual,
                                onValorChange = { viewModel.onComentarioAuditoriaChange(tr.id, it) },
                                etiqueta = "Comentario de Auditoría (Jefe de Servicio)",
                                icono = Icons.AutoMirrored.Filled.Comment,
                                error = errorComentario
                            )

                            // Botones: Aprobar, Rechazar y Abrir Ficha Completa Paso a Paso
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.auditarTraslado(tr.id, aprobar = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TeqSuccessGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aprobar", style = MaterialTheme.typography.labelMedium)
                                }

                                Button(
                                    onClick = { viewModel.auditarTraslado(tr.id, aprobar = false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TeqErrorRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rechazar", style = MaterialTheme.typography.labelMedium)
                                }

                                OutlinedButton(
                                    onClick = { navController.navigate("ficha_detalle/${tr.id}") },
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ficha", style = MaterialTheme.typography.labelMedium)
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
