package com.teqmed.teqruta.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teqmed.teqruta.model.ParametrosTarifa
import com.teqmed.teqruta.model.Usuario
import com.teqmed.teqruta.ui.theme.TeqErrorRed
import com.teqmed.teqruta.ui.theme.TeqSuccessGreen

/**
 * Diálogo de Configuración (Tuerca ⚙️) accesible desde las vistas principales.
 * Muestra datos de sesión activa, estado Offline-First, sincronización y botón de Cerrar Sesión (R.03, R.16).
 */
@Composable
fun ModalConfiguracionTuerca(
    usuario: Usuario?,
    tarifas: ParametrosTarifa,
    pendientesSync: Int,
    sincronizando: Boolean,
    onSincronizarClick: () -> Unit,
    onCerrarSesionClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Configuración y Sesión",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = usuario?.nombre ?: "Usuario Activo",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${usuario?.correo ?: ""} • ${usuario?.rol?.etiqueta ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Parámetros Centralizados Vigentes:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Combustible CNE: $${tarifas.precioCombustibleLitro} CLP / Litro\n" +
                            "• Factor Vehículo Particular: $${tarifas.reembolsoParticularKm} CLP / km\n" +
                            "• Persistencia Local: Activa (Autonomía 8 hrs Offline)",
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider()

                // Botón de sincronización offline con animación (R.16, IE 2.2.2)
                OutlinedButton(
                    onClick = onSincronizarClick,
                    enabled = !sincronizando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (sincronizando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sincronizando registros locales...")
                    } else {
                        Icon(
                            imageVector = if (pendientesSync > 0) Icons.Default.CloudSync else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (pendientesSync > 0) MaterialTheme.colorScheme.primary else TeqSuccessGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (pendientesSync > 0) "Sincronizar ($pendientesSync pendientes)"
                            else "Datos Sincronizados (Al día)"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Botón Cerrar Sesión (R.03)
                Button(
                    onClick = onCerrarSesionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TeqErrorRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar ventana")
            }
        }
    )
}
