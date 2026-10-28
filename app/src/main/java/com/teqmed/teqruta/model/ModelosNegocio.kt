package com.teqmed.teqruta.model

/**
 * Perfiles de usuario definidos en el Caso TeqRuta (TEQMED SpA):
 * - TECNICO: Registra traslados desde el celular al iniciar y finalizar cada visita.
 * - JEFE_SERVICIO: Valida los traslados de su equipo, revisa desviaciones y consulta costos por contrato.
 * - ADMIN: Mantiene actualizados vehículos asignados, destinos (clínicas/hospitales) y tarifas vigentes.
 */
enum class RolUsuario(val etiqueta: String) {
    TECNICO("Técnico de Servicio"),
    JEFE_SERVICIO("Jefe de Servicio / Finanzas"),
    ADMIN("Administrador del Sistema")
}

/**
 * Modelo de Usuario registrado en el sistema (R.01, R.02).
 */
data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: RolUsuario
)

/**
 * Modelo de Vehículo asignado al personal técnico (R.06).
 * Puede ser de la "Empresa" o "Particular".
 */
data class Vehiculo(
    val id: String,
    val patente: String,
    val marcaModelo: String,
    val tipo: String, // "Empresa" o "Particular"
    val rendimientoKmL: Double,
    val trabajadorAsignado: String
)

/**
 * Modelo de Clínica, Hospital o Centro de Diálisis del catastro (R.07).
 * Incluye la distancia oficial del tramo en km para detectar desviaciones (>15%).
 */
data class DestinoClinica(
    val id: String,
    val nombre: String,
    val ciudad: String,
    val direccion: String,
    val distanciaOficialKm: Double,
    val peajeBaseClp: Int,
    val latitudRef: Double,
    val longitudRef: Double
)

/**
 * Parámetros centralizados y auditables de tarifas vigentes (R.08, R.09).
 */
data class ParametrosTarifa(
    val precioCombustibleLitro: Int = 1320,
    val reembolsoParticularKm: Int = 150,
    val ultimaActualizacion: String = "27-10-2026 08:00"
)

/**
 * Estados posibles de auditoría de un traslado (R.20, R.21).
 */
enum class EstadoAuditoria(val etiqueta: String) {
    EN_RUTA("En Ruta"),
    VALIDADO("Validado Automático"),
    CON_DESVIACION("Con Desviación (>15%)"),
    APROBADO("Aprobado por Jefe"),
    RECHAZADO("Rechazado / Observado")
}

/**
 * Registro completo y auditable de un Traslado en Terreno (R.11 a R.24).
 * Guarda un snapshot histórico de la tarifa y distancia oficial vigentes al momento del viaje.
 */
data class Traslado(
    val id: String,
    val tecnicoNombre: String,
    val tecnicoCorreo: String,
    val destinoId: String,
    val destinoNombre: String,
    val destinoCiudad: String,
    val distanciaOficialKm: Double,
    val vehiculoPatente: String,
    val vehiculoMarca: String,
    val vehiculoTipo: String,
    val rendimientoAplicado: Double,
    val tarifaCombustibleAplicada: Int,
    val reembolsoKmAplicado: Int,
    val fechaHoraInicio: String,
    val fechaHoraFin: String = "",
    val odometroInicial: Int,
    val odometroFinal: Int = 0,
    val peajesClp: Int = 0,
    val gpsInicio: String,
    val gpsFin: String = "",
    val fotoUri: String = "",
    val kmDeclarados: Int = 0,
    val porcentajeDesviacion: Double = 0.0,
    val tieneDesviacion: Boolean = false,
    val justificacionTecnico: String = "",
    val costoCombustibleClp: Int = 0,
    val costoDesgasteParticularClp: Int = 0,
    val costoTotalClp: Int = 0,
    val estadoAuditoria: EstadoAuditoria = EstadoAuditoria.EN_RUTA,
    val comentarioAuditoria: String = "",
    val sincronizado: Boolean = false
)
