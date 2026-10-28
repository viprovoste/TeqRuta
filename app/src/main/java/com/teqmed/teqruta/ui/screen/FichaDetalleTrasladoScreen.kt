package com.teqmed.teqruta.ui.screen

import android.net.Uri
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.teqmed.teqruta.ui.theme.TeqNavyDark
import com.teqmed.teqruta.ui.theme.TeqSuccessGreen
import com.teqmed.teqruta.ui.theme.TeqWarningOrange
import com.teqmed.teqruta.viewmodel.TeqRutaViewModel

/**
 * Pantalla 6: Ficha Resumen de Traslado (Proceso P04: R.23, R.24).
 * Diseño limpio y simplificado tipo recibo:
 * 1. Destino, vehículo y estado.
 * 2. Foto capturada y coordenadas GPS.
 * 3. Cálculo paso a paso en tabla simple de 4 líneas (Kilómetros, Combustible, Peajes y Total).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaDetalleTrasladoScreen(
    trasladoId: String,
    navController: NavController,
    viewModel: TeqRutaViewModel
) {
    val traslados by viewModel.traslados.collectAsState()
    val tr = traslados.find { it.id == trasladoId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del Viaje",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TeqNavyDark)
            )
        }
    ) { innerPadding ->
        if (tr == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró el traslado.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Cabecera Simple del Viaje
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr.destinoNombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeqNavyDark,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = if (tr.tieneDesviacion) Color(0xFFFFF3E0) else Color(0xFFE8F8F5),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = tr.estadoAuditoria.etiqueta,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tr.tieneDesviacion) TeqWarningOrange else TeqSuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "${tr.vehiculoPatente} (${tr.vehiculoMarca}) • ${tr.fechaHoraInicio}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }

                // 2. Foto Capturada y Coordenadas GPS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECF0F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tr.fotoUri.startsWith("content://") || tr.fotoUri.startsWith("file://")) {
                                AsyncImage(
                                    model = Uri.parse(tr.fotoUri),
                                    contentDescription = "Foto del traslado",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = TeqNavyDark,
                                        modifier = Modifier.size(38.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Foto de Respaldo Registrada",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TeqNavyDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr.gpsFin.ifBlank { tr.gpsInicio },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                // 3. CÁLCULO PASO A PASO SIMPLE (ESTILO RECIBO LIMPIO)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Resumen del Cálculo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TeqNavyDark
                        )

                        HorizontalDivider()

                        FilaReciboSimple(
                            concepto = "1. Distancia (${tr.odometroInicial} → ${tr.odometroFinal} km)",
                            valor = "${tr.kmDeclarados} km"
                        )

                        FilaReciboSimple(
                            concepto = "2. Distancia Oficial (${if (tr.porcentajeDesviacion >= 0) "+" else ""}${tr.porcentajeDesviacion}%)",
                            valor = "${tr.distanciaOficialKm} km"
                        )

                        FilaReciboSimple(
                            concepto = "3. Combustible ($${tr.tarifaCombustibleAplicada}/L)",
                            valor = "$${tr.costoCombustibleClp}"
                        )

                        if (tr.costoDesgasteParticularClp > 0) {
                            FilaReciboSimple(
                                concepto = "   Reembolso Vehículo Particular",
                                valor = "$${tr.costoDesgasteParticularClp}"
                            )
                        }

                        FilaReciboSimple(
                            concepto = "4. Peajes Pagados",
                            valor = "$${tr.peajesClp}"
                        )

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COSTO TOTAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeqNavyDark
                            )
                            Text(
                                text = "$${tr.costoTotalClp} CLP",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TeqSuccessGreen
                            )
                        }
                    }
                }

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Volver", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FilaReciboSimple(
    concepto: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = concepto,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TeqNavyDark
        )
    }
}
