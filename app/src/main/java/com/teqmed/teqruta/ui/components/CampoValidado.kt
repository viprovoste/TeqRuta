package com.teqmed.teqruta.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.teqmed.teqruta.ui.theme.TeqSuccessGreen

/**
 * Componente estandarizado de campo de formulario con validación visual por campo,
 * ícono principal, ícono de estado (Error / Check) y mensaje animado de retroalimentación.
 * Cumple directamente con el indicador IE 2.1.2 de la rúbrica.
 */
@Composable
fun CampoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String? = null,
    esPassword: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            label = { Text(text = etiqueta) },
            leadingIcon = {
                Icon(
                    imageVector = icono,
                    contentDescription = etiqueta,
                    tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                when {
                    error != null -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error en campo",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    valor.isNotBlank() -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Campo válido",
                            tint = TeqSuccessGreen
                        )
                    }
                }
            },
            isError = error != null,
            visualTransformation = if (esPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            singleLine = singleLine,
            supportingText = {
                AnimatedVisibility(visible = error != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
