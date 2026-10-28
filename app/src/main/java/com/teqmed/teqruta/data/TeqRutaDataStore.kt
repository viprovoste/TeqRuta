package com.teqmed.teqruta.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teqmed.teqruta.model.DestinoClinica
import com.teqmed.teqruta.model.EstadoAuditoria
import com.teqmed.teqruta.model.ParametrosTarifa
import com.teqmed.teqruta.model.RolUsuario
import com.teqmed.teqruta.model.Traslado
import com.teqmed.teqruta.model.Usuario
import com.teqmed.teqruta.model.Vehiculo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

// Instancia única de DataStore (patrón de AnimacionesEstados)
val Context.teqRutaDataStore by preferencesDataStore(name = "preferencias_teqruta_local")

/**
 * Repositorio de Persistencia Local Offline-First basado en Jetpack DataStore (IE 2.3.1, R.17).
 * Permite operar hasta 8 horas sin conexión a internet sin pérdida de datos.
 */
class TeqRutaDataStore(private val context: Context) {

    companion object {
        private val KEY_SESION_CORREO = stringPreferencesKey("sesion_correo")
        private val KEY_USUARIOS_JSON = stringPreferencesKey("usuarios_json")
        private val KEY_VEHICULOS_JSON = stringPreferencesKey("vehiculos_json")
        private val KEY_DESTINOS_JSON = stringPreferencesKey("destinos_json")
        private val KEY_TRASLADOS_JSON = stringPreferencesKey("traslados_json")
        private val KEY_PRECIO_LITRO = intPreferencesKey("precio_combustible_litro")
        private val KEY_REEMBOLSO_KM = intPreferencesKey("reembolso_particular_km")
        private val KEY_FECHA_TARIFA = stringPreferencesKey("fecha_actualizacion_tarifa")
    }

    // ========================================================================
    // 1. GESTIÓN DE SESIÓN ACTIVA (R.03, R.04)
    // ========================================================================
    suspend fun guardarSesionActiva(correo: String?) {
        context.teqRutaDataStore.edit { prefs ->
            if (correo == null) {
                prefs.remove(KEY_SESION_CORREO)
            } else {
                prefs[KEY_SESION_CORREO] = correo
            }
        }
    }

    fun obtenerSesionActivaFlow(): Flow<String?> {
        return context.teqRutaDataStore.data.map { prefs ->
            prefs[KEY_SESION_CORREO]
        }
    }

    // ========================================================================
    // 2. GESTIÓN DE TARIFAS CENTRALIZADAS (R.08, R.09)
    // ========================================================================
    suspend fun guardarTarifas(tarifa: ParametrosTarifa) {
        context.teqRutaDataStore.edit { prefs ->
            prefs[KEY_PRECIO_LITRO] = tarifa.precioCombustibleLitro
            prefs[KEY_REEMBOLSO_KM] = tarifa.reembolsoParticularKm
            prefs[KEY_FECHA_TARIFA] = tarifa.ultimaActualizacion
        }
    }

    fun obtenerTarifasFlow(): Flow<ParametrosTarifa> {
        return context.teqRutaDataStore.data.map { prefs ->
            ParametrosTarifa(
                precioCombustibleLitro = prefs[KEY_PRECIO_LITRO] ?: 1320,
                reembolsoParticularKm = prefs[KEY_REEMBOLSO_KM] ?: 150,
                ultimaActualizacion = prefs[KEY_FECHA_TARIFA] ?: "27-10-2026 08:30 (Fuente CNE Biobío)"
            )
        }
    }

    // ========================================================================
    // 3. GESTIÓN DE USUARIOS (R.01, R.02)
    // ========================================================================
    fun obtenerUsuariosFlow(): Flow<List<Usuario>> {
        return context.teqRutaDataStore.data.map { prefs ->
            val raw = prefs[KEY_USUARIOS_JSON]
            if (raw.isNullOrBlank()) {
                usuariosIniciales()
            } else {
                parsearUsuarios(raw)
            }
        }
    }

    suspend fun guardarUsuarios(lista: List<Usuario>) {
        val arr = JSONArray()
        lista.forEach { u ->
            val obj = JSONObject().apply {
                put("id", u.id)
                put("nombre", u.nombre)
                put("correo", u.correo)
                put("clave", u.clave)
                put("rol", u.rol.name)
            }
            arr.put(obj)
        }
        context.teqRutaDataStore.edit { prefs ->
            prefs[KEY_USUARIOS_JSON] = arr.toString()
        }
    }

    private fun parsearUsuarios(json: String): List<Usuario> {
        return try {
            val arr = JSONArray(json)
            val res = mutableListOf<Usuario>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                res.add(
                    Usuario(
                        id = o.optString("id"),
                        nombre = o.optString("nombre"),
                        correo = o.optString("correo"),
                        clave = o.optString("clave"),
                        rol = RolUsuario.valueOf(o.optString("rol", RolUsuario.TECNICO.name))
                    )
                )
            }
            res
        } catch (_: Exception) {
            usuariosIniciales()
        }
    }

    // ========================================================================
    // 4. GESTIÓN DE VEHÍCULOS (R.06)
    // ========================================================================
    fun obtenerVehiculosFlow(): Flow<List<Vehiculo>> {
        return context.teqRutaDataStore.data.map { prefs ->
            val raw = prefs[KEY_VEHICULOS_JSON]
            if (raw.isNullOrBlank()) {
                vehiculosIniciales()
            } else {
                parsearVehiculos(raw)
            }
        }
    }

    suspend fun guardarVehiculos(lista: List<Vehiculo>) {
        val arr = JSONArray()
        lista.forEach { v ->
            val obj = JSONObject().apply {
                put("id", v.id)
                put("patente", v.patente)
                put("marcaModelo", v.marcaModelo)
                put("tipo", v.tipo)
                put("rendimientoKmL", v.rendimientoKmL)
                put("trabajadorAsignado", v.trabajadorAsignado)
            }
            arr.put(obj)
        }
        context.teqRutaDataStore.edit { prefs ->
            prefs[KEY_VEHICULOS_JSON] = arr.toString()
        }
    }

    private fun parsearVehiculos(json: String): List<Vehiculo> {
        return try {
            val arr = JSONArray(json)
            val res = mutableListOf<Vehiculo>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                res.add(
                    Vehiculo(
                        id = o.optString("id"),
                        patente = o.optString("patente"),
                        marcaModelo = o.optString("marcaModelo"),
                        tipo = o.optString("tipo", "Empresa"),
                        rendimientoKmL = o.optDouble("rendimientoKmL", 12.0),
                        trabajadorAsignado = o.optString("trabajadorAsignado")
                    )
                )
            }
            res
        } catch (_: Exception) {
            vehiculosIniciales()
        }
    }

    // ========================================================================
    // 5. GESTIÓN DE DESTINOS / CLÍNICAS (R.07)
    // ========================================================================
    fun obtenerDestinosFlow(): Flow<List<DestinoClinica>> {
        return context.teqRutaDataStore.data.map { prefs ->
            val raw = prefs[KEY_DESTINOS_JSON]
            if (raw.isNullOrBlank()) {
                destinosIniciales()
            } else {
                parsearDestinos(raw)
            }
        }
    }

    suspend fun guardarDestinos(lista: List<DestinoClinica>) {
        val arr = JSONArray()
        lista.forEach { d ->
            val obj = JSONObject().apply {
                put("id", d.id)
                put("nombre", d.nombre)
                put("ciudad", d.ciudad)
                put("direccion", d.direccion)
                put("distanciaOficialKm", d.distanciaOficialKm)
                put("peajeBaseClp", d.peajeBaseClp)
                put("latitudRef", d.latitudRef)
                put("longitudRef", d.longitudRef)
            }
            arr.put(obj)
        }
        context.teqRutaDataStore.edit { prefs ->
            prefs[KEY_DESTINOS_JSON] = arr.toString()
        }
    }

    private fun parsearDestinos(json: String): List<DestinoClinica> {
        return try {
            val arr = JSONArray(json)
            val res = mutableListOf<DestinoClinica>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                res.add(
                    DestinoClinica(
                        id = o.optString("id"),
                        nombre = o.optString("nombre"),
                        ciudad = o.optString("ciudad"),
                        direccion = o.optString("direccion"),
                        distanciaOficialKm = o.optDouble("distanciaOficialKm", 10.0),
                        peajeBaseClp = o.optInt("peajeBaseClp", 0),
                        latitudRef = o.optDouble("latitudRef", -36.8269),
                        longitudRef = o.optDouble("longitudRef", -73.0497)
                    )
                )
            }
            res
        } catch (_: Exception) {
            destinosIniciales()
        }
    }

    // ========================================================================
    // 6. GESTIÓN DE TRASLADOS EN TERRENO (R.11 a R.24)
    // ========================================================================
    fun obtenerTrasladosFlow(): Flow<List<Traslado>> {
        return context.teqRutaDataStore.data.map { prefs ->
            val raw = prefs[KEY_TRASLADOS_JSON]
            if (raw.isNullOrBlank()) {
                trasladosIniciales()
            } else {
                parsearTraslados(raw)
            }
        }
    }

    suspend fun guardarTraslados(lista: List<Traslado>) {
        val arr = JSONArray()
        lista.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("tecnicoNombre", t.tecnicoNombre)
                put("tecnicoCorreo", t.tecnicoCorreo)
                put("destinoId", t.destinoId)
                put("destinoNombre", t.destinoNombre)
                put("destinoCiudad", t.destinoCiudad)
                put("distanciaOficialKm", t.distanciaOficialKm)
                put("vehiculoPatente", t.vehiculoPatente)
                put("vehiculoMarca", t.vehiculoMarca)
                put("vehiculoTipo", t.vehiculoTipo)
                put("rendimientoAplicado", t.rendimientoAplicado)
                put("tarifaCombustibleAplicada", t.tarifaCombustibleAplicada)
                put("reembolsoKmAplicado", t.reembolsoKmAplicado)
                put("fechaHoraInicio", t.fechaHoraInicio)
                put("fechaHoraFin", t.fechaHoraFin)
                put("odometroInicial", t.odometroInicial)
                put("odometroFinal", t.odometroFinal)
                put("peajesClp", t.peajesClp)
                put("gpsInicio", t.gpsInicio)
                put("gpsFin", t.gpsFin)
                put("fotoUri", t.fotoUri)
                put("kmDeclarados", t.kmDeclarados)
                put("porcentajeDesviacion", t.porcentajeDesviacion)
                put("tieneDesviacion", t.tieneDesviacion)
                put("justificacionTecnico", t.justificacionTecnico)
                put("costoCombustibleClp", t.costoCombustibleClp)
                put("costoDesgasteParticularClp", t.costoDesgasteParticularClp)
                put("costoTotalClp", t.costoTotalClp)
                put("estadoAuditoria", t.estadoAuditoria.name)
                put("comentarioAuditoria", t.comentarioAuditoria)
                put("sincronizado", t.sincronizado)
            }
            arr.put(obj)
        }
        context.teqRutaDataStore.edit { prefs ->
            prefs[KEY_TRASLADOS_JSON] = arr.toString()
        }
    }

    private fun parsearTraslados(json: String): List<Traslado> {
        return try {
            val arr = JSONArray(json)
            val res = mutableListOf<Traslado>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                res.add(
                    Traslado(
                        id = o.optString("id"),
                        tecnicoNombre = o.optString("tecnicoNombre"),
                        tecnicoCorreo = o.optString("tecnicoCorreo"),
                        destinoId = o.optString("destinoId"),
                        destinoNombre = o.optString("destinoNombre"),
                        destinoCiudad = o.optString("destinoCiudad"),
                        distanciaOficialKm = o.optDouble("distanciaOficialKm", 10.0),
                        vehiculoPatente = o.optString("vehiculoPatente"),
                        vehiculoMarca = o.optString("vehiculoMarca"),
                        vehiculoTipo = o.optString("vehiculoTipo"),
                        rendimientoAplicado = o.optDouble("rendimientoAplicado", 14.0),
                        tarifaCombustibleAplicada = o.optInt("tarifaCombustibleAplicada", 1320),
                        reembolsoKmAplicado = o.optInt("reembolsoKmAplicado", 150),
                        fechaHoraInicio = o.optString("fechaHoraInicio"),
                        fechaHoraFin = o.optString("fechaHoraFin"),
                        odometroInicial = o.optInt("odometroInicial"),
                        odometroFinal = o.optInt("odometroFinal"),
                        peajesClp = o.optInt("peajesClp"),
                        gpsInicio = o.optString("gpsInicio"),
                        gpsFin = o.optString("gpsFin"),
                        fotoUri = o.optString("fotoUri"),
                        kmDeclarados = o.optInt("kmDeclarados"),
                        porcentajeDesviacion = o.optDouble("porcentajeDesviacion"),
                        tieneDesviacion = o.optBoolean("tieneDesviacion"),
                        justificacionTecnico = o.optString("justificacionTecnico"),
                        costoCombustibleClp = o.optInt("costoCombustibleClp"),
                        costoDesgasteParticularClp = o.optInt("costoDesgasteParticularClp"),
                        costoTotalClp = o.optInt("costoTotalClp"),
                        estadoAuditoria = EstadoAuditoria.valueOf(
                            o.optString("estadoAuditoria", EstadoAuditoria.VALIDADO.name)
                        ),
                        comentarioAuditoria = o.optString("comentarioAuditoria"),
                        sincronizado = o.optBoolean("sincronizado")
                    )
                )
            }
            res
        } catch (_: Exception) {
            trasladosIniciales()
        }
    }

    // ========================================================================
    // DATOS SEMILLA (SEED DATA) DEL CASO TEQMED SpA (REGIÓN DEL BIOBÍO)
    // ========================================================================
    fun usuariosIniciales(): List<Usuario> = listOf(
        Usuario(
            id = "U01",
            nombre = "Juan Pérez (Técnico)",
            correo = "juan.perez@teqmed.cl",
            clave = "123456",
            rol = RolUsuario.TECNICO
        ),
        Usuario(
            id = "U02",
            nombre = "Carlos Soto (Jefe Servicio)",
            correo = "jefe@teqmed.cl",
            clave = "123456",
            rol = RolUsuario.JEFE_SERVICIO
        ),
        Usuario(
            id = "U03",
            nombre = "Ana Rivas (Administrador)",
            correo = "admin@teqmed.cl",
            clave = "123456",
            rol = RolUsuario.ADMIN
        )
    )

    fun vehiculosIniciales(): List<Vehiculo> = listOf(
        Vehiculo(
            id = "V01",
            patente = "KXYZ-89",
            marcaModelo = "Mitsubishi L200 Work",
            tipo = "Empresa",
            rendimientoKmL = 14.0,
            trabajadorAsignado = "Juan Pérez (Técnico)"
        ),
        Vehiculo(
            id = "V02",
            patente = "PQRST-12",
            marcaModelo = "Peugeot Partner Furgón",
            tipo = "Empresa",
            rendimientoKmL = 16.5,
            trabajadorAsignado = "Diego Muñoz (Técnico)"
        ),
        Vehiculo(
            id = "V03",
            patente = "ABCD-34",
            marcaModelo = "Suzuki Swift 1.2",
            tipo = "Particular",
            rendimientoKmL = 18.0,
            trabajadorAsignado = "Juan Pérez (Técnico)"
        )
    )

    fun destinosIniciales(): List<DestinoClinica> = listOf(
        DestinoClinica(
            id = "D01",
            nombre = "Hospital Regional Guillermo Grant Benavente",
            ciudad = "Concepción",
            direccion = "San Martín 1436, Concepción",
            distanciaOficialKm = 6.0,
            peajeBaseClp = 0,
            latitudRef = -36.8225,
            longitudRef = -73.0394
        ),
        DestinoClinica(
            id = "D02",
            nombre = "Clínica Biobío (Centro Diálisis)",
            ciudad = "Talcahuano",
            direccion = "Av. Jorge Alessandri 3515",
            distanciaOficialKm = 11.0,
            peajeBaseClp = 0,
            latitudRef = -36.7832,
            longitudRef = -73.0681
        ),
        DestinoClinica(
            id = "D03",
            nombre = "Clínica Andes Salud Chillán",
            ciudad = "Chillán",
            direccion = "Av. Libertad 745, Chillán",
            distanciaOficialKm = 102.0,
            peajeBaseClp = 5400,
            latitudRef = -36.6063,
            longitudRef = -72.1023
        ),
        DestinoClinica(
            id = "D04",
            nombre = "Complejo Asistencial Dr. Víctor Ríos Ruiz",
            ciudad = "Los Ángeles",
            direccion = "Av. Ricardo Vicuña 147, Los Ángeles",
            distanciaOficialKm = 128.0,
            peajeBaseClp = 3200,
            latitudRef = -37.4697,
            longitudRef = -72.3537
        ),
        DestinoClinica(
            id = "D05",
            nombre = "Clínica Alemana de Temuco",
            ciudad = "Temuco",
            direccion = "Senador Estébanez 645, Temuco",
            distanciaOficialKm = 290.0,
            peajeBaseClp = 8600,
            latitudRef = -38.7359,
            longitudRef = -72.6080
        )
    )

    fun trasladosIniciales(): List<Traslado> = listOf(
        Traslado(
            id = "TR-1001",
            tecnicoNombre = "Juan Pérez (Técnico)",
            tecnicoCorreo = "juan.perez@teqmed.cl",
            destinoId = "D03",
            destinoNombre = "Clínica Andes Salud Chillán",
            destinoCiudad = "Chillán",
            distanciaOficialKm = 102.0,
            vehiculoPatente = "KXYZ-89",
            vehiculoMarca = "Mitsubishi L200 Work",
            vehiculoTipo = "Empresa",
            rendimientoAplicado = 14.0,
            tarifaCombustibleAplicada = 1320,
            reembolsoKmAplicado = 0,
            fechaHoraInicio = "27-10-2026 08:15",
            fechaHoraFin = "27-10-2026 11:40",
            odometroInicial = 45210,
            odometroFinal = 45335,
            peajesClp = 5400,
            gpsInicio = "Lat: -36.8269, Lon: -73.0497 (Sede Concepción)",
            gpsFin = "Lat: -36.6063, Lon: -72.1023 (Chillán)",
            fotoUri = "respaldo_odometro_chillan",
            kmDeclarados = 125,
            porcentajeDesviacion = 22.5,
            tieneDesviacion = true,
            justificacionTecnico = "Corte en Ruta Itata por obras viales, desvío obligatorio por sector Bulnes (+23 km).",
            costoCombustibleClp = 11786,
            costoDesgasteParticularClp = 0,
            costoTotalClp = 17186,
            estadoAuditoria = EstadoAuditoria.CON_DESVIACION,
            comentarioAuditoria = "",
            sincronizado = false
        ),
        Traslado(
            id = "TR-1002",
            tecnicoNombre = "Juan Pérez (Técnico)",
            tecnicoCorreo = "juan.perez@teqmed.cl",
            destinoId = "D01",
            destinoNombre = "Hospital Regional Guillermo Grant Benavente",
            destinoCiudad = "Concepción",
            distanciaOficialKm = 6.0,
            vehiculoPatente = "KXYZ-89",
            vehiculoMarca = "Mitsubishi L200 Work",
            vehiculoTipo = "Empresa",
            rendimientoAplicado = 14.0,
            tarifaCombustibleAplicada = 1320,
            reembolsoKmAplicado = 0,
            fechaHoraInicio = "26-10-2026 14:10",
            fechaHoraFin = "26-10-2026 16:30",
            odometroInicial = 45204,
            odometroFinal = 45210,
            peajesClp = 0,
            gpsInicio = "Lat: -36.8269, Lon: -73.0497 (Sede Concepción)",
            gpsFin = "Lat: -36.8225, Lon: -73.0394 (Concepción)",
            fotoUri = "respaldo_odometro_regional",
            kmDeclarados = 6,
            porcentajeDesviacion = 0.0,
            tieneDesviacion = false,
            justificacionTecnico = "Recorrido directo sin incidentes.",
            costoCombustibleClp = 566,
            costoDesgasteParticularClp = 0,
            costoTotalClp = 566,
            estadoAuditoria = EstadoAuditoria.VALIDADO,
            comentarioAuditoria = "Validado automáticamente (0% desviación).",
            sincronizado = true
        )
    )
}
