package com.teqmed.teqruta.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teqmed.teqruta.model.Traslado
import com.teqmed.teqruta.ui.theme.TeqNavyDark
import com.teqmed.teqruta.ui.theme.TeqSuccessGreen
import com.teqmed.teqruta.ui.theme.TeqWarningOrange

/**
 * Componente visual de Mapa Cartográfico / Radar GPS para la pantalla principal del conductor.
 * Muestra la ubicación actual de la persona (coordenadas GPS reales del dispositivo),
 * la Sede de Concepción y el trazado hacia la clínica destino si hay un viaje en curso.
 * Opera 100% sin conexión (Offline-First) y sin depender de API Keys externas.
 */
@Composable
fun MapaRadarGps(
    coordenadasGps: String,
    viajeEnCurso: Traslado?,
    onActualizarGpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación continua de pulso para el marcador GPS de la persona (IE 2.2.2)
    val infiniteTransition = rememberInfiniteTransition(label = "pulso_gps")
    val radioPulso by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radio_pulso"
    )
    val alfaPulso by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alfa_pulso"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F1F5))
        ) {
            // Lienzo Cartográfico Ruta Biobío (Concepción - Chillán - Los Ángeles - Temuco)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Grilla de coordenadas de mapa
                val gridStep = 65f
                var x = 0f
                while (x < w) {
                    drawLine(
                        color = Color(0xFFD0E2EC),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.5f
                    )
                    x += gridStep
                }
                var y = 0f
                while (y < h) {
                    drawLine(
                        color = Color(0xFFD0E2EC),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.5f
                    )
                    y += gridStep
                }

                // Río Biobío estilizado
                drawLine(
                    color = Color(0xFFB3D9FF),
                    start = Offset(0f, h * 0.65f),
                    end = Offset(w, h * 0.42f),
                    strokeWidth = 28f
                )

                // Autopistas principales (Ruta 152 Itata / Ruta 5 Sur)
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.25f, h * 0.58f),
                    end = Offset(w * 0.78f, h * 0.24f),
                    strokeWidth = 10f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.25f, h * 0.58f),
                    end = Offset(w * 0.75f, h * 0.82f),
                    strokeWidth = 10f
                )

                val posPersona = Offset(w * 0.34f, h * 0.54f)
                val posDestino = Offset(w * 0.76f, h * 0.28f)

                // Si hay viaje activo, dibuja la ruta punteada activa hacia la clínica
                if (viajeEnCurso != null) {
                    drawLine(
                        color = TeqWarningOrange,
                        start = posPersona,
                        end = posDestino,
                        strokeWidth = 7f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                    )
                    // Pin Clínica Destino
                    drawCircle(
                        color = TeqWarningOrange,
                        radius = 16f,
                        center = posDestino
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7f,
                        center = posDestino
                    )
                }

                // Pulso animado donde se encuentra la persona (GPS)
                drawCircle(
                    color = Color(0xFF0A4D7C).copy(alpha = alfaPulso),
                    radius = radioPulso,
                    center = posPersona
                )
                drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = posPersona
                )
                drawCircle(
                    color = TeqNavyDark,
                    radius = 11f,
                    center = posPersona
                )
            }

            // Etiqueta superior sobre el mapa con coordenadas GPS en vivo
            Surface(
                color = Color.White.copy(alpha = 0.94f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación GPS",
                            tint = TeqNavyDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Tu Ubicación Actual (GPS Nativo)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeqNavyDark
                            )
                            Text(
                                text = coordenadasGps,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                    FilledTonalIconButton(onClick = onActualizarGpsClick) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "Actualizar GPS"
                        )
                    }
                }
            }

            // Etiqueta inferior dentro del mapa con el estado de ruta actual
            Surface(
                color = if (viajeEnCurso != null) Color(0xFFFFF3E0) else Color.White.copy(alpha = 0.94f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viajeEnCurso != null) TeqWarningOrange else TeqSuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    if (viajeEnCurso != null) {
                        Column {
                            Text(
                                text = "EN RUTA A: ${viajeEnCurso.destinoNombre}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TeqWarningOrange
                            )
                            Text(
                                text = "Vehículo: ${viajeEnCurso.vehiculoPatente} • Odómetro Inicial: ${viajeEnCurso.odometroInicial} km • Dist. Oficial: ${viajeEnCurso.distanciaOficialKm} km",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Conductor Disponible en Terreno (Sede Concepción)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TeqSuccessGreen
                            )
                            Text(
                                text = "Presiona el botón 'INICIO' abajo para abrir un nuevo traslado hacia una clínica.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
