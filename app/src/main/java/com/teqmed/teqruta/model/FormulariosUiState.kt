package com.teqmed.teqruta.model

/**
 * Modelos de Estado de Interfaz (UiState) y Errores por Campo para todos los formularios.
 * Basado en el patrón desacoplado de FormulariosKotlinDuoc (IE 2.1.2 e IE 2.2.1).
 */

// ============================================================================
// 1. FORMULARIO DE LOGIN Y REGISTRO DE USUARIOS (P01: R.01 a R.05)
// ============================================================================
data class LoginErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val general: String? = null
)

data class LoginUiState(
    val esModoRegistro: Boolean = false,
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val rolSeleccionado: RolUsuario = RolUsuario.TECNICO,
    val errores: LoginErrores = LoginErrores()
)

// ============================================================================
// 2. FORMULARIO DE INICIO Y FINALIZACIÓN DE VIAJE (P03: R.11 a R.15, R.20)
// ============================================================================
data class ViajeErrores(
    val destinoId: String? = null,
    val vehiculoPatente: String? = null,
    val odometroInicial: String? = null,
    val odometroFinal: String? = null,
    val peajesClp: String? = null,
    val justificacion: String? = null,
    val fotoRespaldo: String? = null
)

data class ViajeUiState(
    val destinoId: String = "",
    val vehiculoPatente: String = "",
    val odometroInicial: String = "",
    val odometroFinal: String = "",
    val peajesClp: String = "0",
    val justificacion: String = "",
    val fotoUri: String = "",
    val gpsActual: String = "Lat: -36.8269, Lon: -73.0497 (Sede Concepción)",
    val errores: ViajeErrores = ViajeErrores()
)

// ============================================================================
// 3. FORMULARIOS DEL PANEL DE ADMINISTRADOR (P02: R.06 a R.10)
// ============================================================================
data class VehiculoErrores(
    val patente: String? = null,
    val marcaModelo: String? = null,
    val rendimientoKmL: String? = null,
    val trabajadorAsignado: String? = null
)

data class VehiculoFormUiState(
    val patente: String = "",
    val marcaModelo: String = "",
    val tipo: String = "Empresa",
    val rendimientoKmL: String = "",
    val trabajadorAsignado: String = "",
    val errores: VehiculoErrores = VehiculoErrores()
)

data class DestinoErrores(
    val nombre: String? = null,
    val ciudad: String? = null,
    val direccion: String? = null,
    val distanciaOficialKm: String? = null,
    val peajeBaseClp: String? = null
)

data class DestinoFormUiState(
    val nombre: String = "",
    val ciudad: String = "Concepción",
    val direccion: String = "",
    val distanciaOficialKm: String = "",
    val peajeBaseClp: String = "0",
    val errores: DestinoErrores = DestinoErrores()
)

data class TarifaErrores(
    val precioCombustibleLitro: String? = null,
    val reembolsoParticularKm: String? = null
)

data class TarifaFormUiState(
    val precioCombustibleLitro: String = "1320",
    val reembolsoParticularKm: String = "150",
    val errores: TarifaErrores = TarifaErrores()
)
