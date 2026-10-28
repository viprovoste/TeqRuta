package com.teqmed.teqruta.ui.screen

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teqmed.teqruta.model.RolUsuario
import com.teqmed.teqruta.ui.components.CampoValidado
import com.teqmed.teqruta.ui.theme.TeqNavyDark
import com.teqmed.teqruta.ui.theme.TeqNavyPrimary
import com.teqmed.teqruta.ui.theme.TeqTealSecondary
import com.teqmed.teqruta.viewmodel.TeqRutaViewModel

/**
 * Pantalla 1: Login y Registro de Usuarios (Proceso P01: R.01 a R.05).
 * Permite iniciar sesión o registrar nuevos usuarios eligiendo su rol en TEQMED SpA.
 */
@Composable
fun LoginRegistroScreen(
    navController: NavController,
    viewModel: TeqRutaViewModel
) {
    val estado by viewModel.loginState.collectAsState()

    val colorBoton by animateColorAsState(
        targetValue = if (estado.esModoRegistro) TeqTealSecondary else TeqNavyPrimary,
        animationSpec = tween(durationMillis = 400),
        label = "color_boton_login"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Cabecera Institucional TEQMED SpA
        Surface(
            color = TeqNavyDark,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Logo TeqRuta",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TeqRuta – TEQMED SpA",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TeqNavyDark
        )
        Text(
            text = "Control y Auditoría de Traslados en Terreno (Región del Biobío)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Pestañas para alternar entre Iniciar Sesión y Registrarse
                TabRow(selectedTabIndex = if (estado.esModoRegistro) 1 else 0) {
                    Tab(
                        selected = !estado.esModoRegistro,
                        onClick = { viewModel.onModoRegistroChange(false) },
                        text = { Text("Iniciar Sesión", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = estado.esModoRegistro,
                        onClick = { viewModel.onModoRegistroChange(true) },
                        text = { Text("Registrar Usuario", fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Nombre (Visible con animación cuando está en modo Registro)
                AnimatedVisibility(visible = estado.esModoRegistro) {
                    Column {
                        CampoValidado(
                            valor = estado.nombre,
                            onValorChange = viewModel::onLoginNombreChange,
                            etiqueta = "Nombre Completo del Trabajador",
                            icono = Icons.Default.Badge,
                            error = estado.errores.nombre
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Campo Usuario / Correo
                CampoValidado(
                    valor = estado.correo,
                    onValorChange = viewModel::onLoginCorreoChange,
                    etiqueta = "Usuario / Correo (@teqmed.cl)",
                    icono = Icons.Default.Email,
                    error = estado.errores.correo,
                    tipoTeclado = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo Contraseña
                CampoValidado(
                    valor = estado.clave,
                    onValorChange = viewModel::onLoginClaveChange,
                    etiqueta = "Contraseña (mín. 6 caracteres)",
                    icono = Icons.Default.Lock,
                    error = estado.errores.clave,
                    esPassword = true,
                    tipoTeclado = KeyboardType.Password
                )

                // Selector de Rol al registrar un nuevo usuario
                AnimatedVisibility(visible = estado.esModoRegistro) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "Seleccione el Perfil del Usuario:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            RolUsuario.entries.forEach { rol ->
                                FilterChip(
                                    selected = estado.rolSeleccionado == rol,
                                    onClick = { viewModel.onLoginRolChange(rol) },
                                    label = { Text(rol.etiqueta) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Mensaje de error general animado (Credenciales inválidas)
                AnimatedVisibility(visible = estado.errores.general != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color(0xFFFDEDEC), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = estado.errores.general ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val usuarioAutenticado = viewModel.validarAutenticacion()
                        if (usuarioAutenticado != null) {
                            val rutaDestino = when (usuarioAutenticado.rol) {
                                RolUsuario.TECNICO -> "mapa_conductor"
                                RolUsuario.JEFE_SERVICIO -> "jefe_servicio"
                                RolUsuario.ADMIN -> "panel_admin"
                            }
                            navController.navigate(rutaDestino) {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorBoton),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (estado.esModoRegistro) "REGISTRAR E INGRESAR" else "INGRESAR A TEQRUTA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Tarjeta de Acceso Rápido para probar los 3 Perfiles del Caso
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cuentas Pre-cargadas (Toque para autocompletar):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeqNavyDark
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(
                        onClick = { viewModel.rellenarCredencialesDemo(RolUsuario.TECNICO) },
                        label = { Text("👨‍🔧 Técnico") }
                    )
                    AssistChip(
                        onClick = { viewModel.rellenarCredencialesDemo(RolUsuario.JEFE_SERVICIO) },
                        label = { Text("🕵️ Jefe Serv.") }
                    )
                    AssistChip(
                        onClick = { viewModel.rellenarCredencialesDemo(RolUsuario.ADMIN) },
                        label = { Text("⚙️ Admin") }
                    )
                }
            }
        }
    }
}
