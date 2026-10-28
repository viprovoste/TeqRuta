package com.teqmed.teqruta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teqmed.teqruta.data.TeqRutaDataStore
import com.teqmed.teqruta.model.DestinoClinica
import com.teqmed.teqruta.model.DestinoErrores
import com.teqmed.teqruta.model.DestinoFormUiState
import com.teqmed.teqruta.model.EstadoAuditoria
import com.teqmed.teqruta.model.LoginErrores
import com.teqmed.teqruta.model.LoginUiState
import com.teqmed.teqruta.model.ParametrosTarifa
import com.teqmed.teqruta.model.RolUsuario
import com.teqmed.teqruta.model.TarifaErrores
import com.teqmed.teqruta.model.TarifaFormUiState
import com.teqmed.teqruta.model.Traslado
import com.teqmed.teqruta.model.Usuario
import com.teqmed.teqruta.model.Vehiculo
import com.teqmed.teqruta.model.VehiculoErrores
import com.teqmed.teqruta.model.VehiculoFormUiState
import com.teqmed.teqruta.model.ViajeErrores
import com.teqmed.teqruta.model.ViajeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * ViewModel central que gestiona toda la lógica de negocio y validaciones desacopladas de la UI.
 * Combina el patrón de EstadoViewModel (AnimacionesEstados) y UsuarioViewModel (FormulariosKotlinDuoc).
 * Cumple con IE 2.1.2, IE 2.2.1, IE 2.2.2 e IE 2.3.1.
 */
class TeqRutaViewModel(application: Application) : AndroidViewModel(application) {

    // 🗃️ Repositorio local DataStore (Persistencia Offline-First)
    private val dataStore = TeqRutaDataStore(application)

    // 🔄 Estado de carga inicial y notificaciones animadas (Patrón AnimacionesEstados)
    private val _cargandoDatos = MutableStateFlow(true)
    val cargandoDatos: StateFlow<Boolean> = _cargandoDatos

    private val _mensajeExitoAnimado = MutableStateFlow<String?>(null)
    val mensajeExitoAnimado: StateFlow<String?> = _mensajeExitoAnimado

    private val _sincronizandoOffline = MutableStateFlow(false)
    val sincronizandoOffline: StateFlow<Boolean> = _sincronizandoOffline

    // 👤 Estado del usuario autenticado actualmente
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual

    // 📋 Listas reactivas persistidas localmente
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _vehiculos = MutableStateFlow<List<Vehiculo>>(emptyList())
    val vehiculos: StateFlow<List<Vehiculo>> = _vehiculos

    private val _destinos = MutableStateFlow<List<DestinoClinica>>(emptyList())
    val destinos: StateFlow<List<DestinoClinica>> = _destinos

    private val _tarifas = MutableStateFlow(ParametrosTarifa())
    val tarifas: StateFlow<ParametrosTarifa> = _tarifas

    private val _traslados = MutableStateFlow<List<Traslado>>(emptyList())
    val traslados: StateFlow<List<Traslado>> = _traslados

    // 📝 Estados de los Formularios (Patrón FormulariosKotlinDuoc)
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState

    private val _viajeState = MutableStateFlow(ViajeUiState())
    val viajeState: StateFlow<ViajeUiState> = _viajeState

    private val _vehiculoFormState = MutableStateFlow(VehiculoFormUiState())
    val vehiculoFormState: StateFlow<VehiculoFormUiState> = _vehiculoFormState

    private val _destinoFormState = MutableStateFlow(DestinoFormUiState())
    val destinoFormState: StateFlow<DestinoFormUiState> = _destinoFormState

    private val _tarifaFormState = MutableStateFlow(TarifaFormUiState())
    val tarifaFormState: StateFlow<TarifaFormUiState> = _tarifaFormState

    // Filtros y comentarios de auditoría en la pantalla del Jefe de Servicio
    private val _filtroCliente = MutableStateFlow("Todos")
    val filtroCliente: StateFlow<String> = _filtroCliente

    private val _filtroVehiculo = MutableStateFlow("Todos")
    val filtroVehiculo: StateFlow<String> = _filtroVehiculo

    private val _comentariosAuditoria = MutableStateFlow<Map<String, String>>(emptyMap())
    val comentariosAuditoria: StateFlow<Map<String, String>> = _comentariosAuditoria

    private val _erroresAuditoria = MutableStateFlow<Map<String, String>>(emptyMap())
    val erroresAuditoria: StateFlow<Map<String, String>> = _erroresAuditoria

    init {
        cargarDatosLocales()
    }

    private fun cargarDatosLocales() {
        viewModelScope.launch {
            val listaUsuarios = dataStore.obtenerUsuariosFlow().first()
            val listaVehiculos = dataStore.obtenerVehiculosFlow().first()
            val listaDestinos = dataStore.obtenerDestinosFlow().first()
            val tarifaActual = dataStore.obtenerTarifasFlow().first()
            val listaTraslados = dataStore.obtenerTrasladosFlow().first()
            val sesionCorreo = dataStore.obtenerSesionActivaFlow().first()

            _usuarios.value = listaUsuarios
            _vehiculos.value = listaVehiculos
            _destinos.value = listaDestinos
            _tarifas.value = tarifaActual
            _traslados.value = listaTraslados

            _tarifaFormState.update {
                it.copy(
                    precioCombustibleLitro = tarifaActual.precioCombustibleLitro.toString(),
                    reembolsoParticularKm = tarifaActual.reembolsoParticularKm.toString()
                )
            }

            // Pre-seleccionar primer destino y vehículo por defecto
            if (listaDestinos.isNotEmpty() && _viajeState.value.destinoId.isBlank()) {
                _viajeState.update { it.copy(destinoId = listaDestinos.first().id) }
            }
            if (listaVehiculos.isNotEmpty() && _viajeState.value.vehiculoPatente.isBlank()) {
                _viajeState.update { it.copy(vehiculoPatente = listaVehiculos.first().patente) }
            }

            if (!sesionCorreo.isNullOrBlank()) {
                _usuarioActual.value = listaUsuarios.find {
                    it.correo.equals(sesionCorreo, ignoreCase = true)
                }
            }
            _cargandoDatos.value = false
        }
    }

    fun mostrarFeedbackAnimado(mensaje: String) {
        viewModelScope.launch {
            _mensajeExitoAnimado.value = mensaje
            delay(2800)
            _mensajeExitoAnimado.value = null
        }
    }

    // ========================================================================
    // PROCESO P01: LOGIN, REGISTRO Y GESTIÓN DE SESIÓN (R.01 a R.05)
    // ========================================================================
    fun onModoRegistroChange(esRegistro: Boolean) {
        _loginState.update { it.copy(esModoRegistro = esRegistro, errores = LoginErrores()) }
    }

    fun onLoginNombreChange(valor: String) {
        _loginState.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null, general = null)) }
    }

    fun onLoginCorreoChange(valor: String) {
        _loginState.update { it.copy(correo = valor, errores = it.errores.copy(correo = null, general = null)) }
    }

    fun onLoginClaveChange(valor: String) {
        _loginState.update { it.copy(clave = valor, errores = it.errores.copy(clave = null, general = null)) }
    }

    fun onLoginRolChange(rol: RolUsuario) {
        _loginState.update { it.copy(rolSeleccionado = rol) }
    }

    fun rellenarCredencialesDemo(rol: RolUsuario) {
        val cuenta = _usuarios.value.find { it.rol == rol } ?: return
        _loginState.update {
            it.copy(
                esModoRegistro = false,
                correo = cuenta.correo,
                clave = cuenta.clave,
                rolSeleccionado = rol,
                errores = LoginErrores()
            )
        }
    }

    fun validarAutenticacion(): Usuario? {
        val st = _loginState.value
        val correoLimpio = st.correo.trim()

        val errorNombre = if (st.esModoRegistro && st.nombre.isBlank()) {
            "El nombre completo es obligatorio para registrarse"
        } else null

        val errorCorreo = when {
            correoLimpio.isBlank() -> "Debe ingresar su usuario o correo electrónico"
            !correoLimpio.contains("@") || !correoLimpio.contains(".") -> "Formato de correo inválido (ej. usuario@teqmed.cl)"
            else -> null
        }

        val errorClave = when {
            st.clave.isBlank() -> "Debe ingresar su contraseña"
            st.clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        if (errorNombre != null || errorCorreo != null || errorClave != null) {
            _loginState.update {
                it.copy(errores = LoginErrores(nombre = errorNombre, correo = errorCorreo, clave = errorClave))
            }
            return null
        }

        if (st.esModoRegistro) {
            val yaExiste = _usuarios.value.any { it.correo.equals(correoLimpio, ignoreCase = true) }
            if (yaExiste) {
                _loginState.update {
                    it.copy(errores = LoginErrores(correo = "Este correo ya está registrado en el sistema"))
                }
                return null
            }

            val nuevoUsuario = Usuario(
                id = "U0${_usuarios.value.size + 1}",
                nombre = st.nombre.trim(),
                correo = correoLimpio,
                clave = st.clave,
                rol = st.rolSeleccionado
            )
            val nuevaLista = _usuarios.value + nuevoUsuario
            _usuarios.value = nuevaLista
            _usuarioActual.value = nuevoUsuario

            viewModelScope.launch {
                dataStore.guardarUsuarios(nuevaLista)
                dataStore.guardarSesionActiva(nuevoUsuario.correo)
            }
            mostrarFeedbackAnimado("¡Usuario registrado con éxito como ${nuevoUsuario.rol.etiqueta}!")
            return nuevoUsuario
        } else {
            val encontrado = _usuarios.value.find {
                it.correo.equals(correoLimpio, ignoreCase = true) && it.clave == st.clave
            }
            if (encontrado == null) {
                _loginState.update {
                    it.copy(errores = LoginErrores(general = "Credenciales incorrectas. Verifique usuario y contraseña."))
                }
                return null
            }

            _usuarioActual.value = encontrado
            viewModelScope.launch {
                dataStore.guardarSesionActiva(encontrado.correo)
            }
            return encontrado
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        _loginState.value = LoginUiState()
        viewModelScope.launch {
            dataStore.guardarSesionActiva(null)
        }
    }

    // ========================================================================
    // PROCESO P03: REGISTRO OPERATIVO DE TRASLADOS EN TERRENO (R.11 a R.19)
    // ========================================================================
    fun obtenerViajeEnCurso(): Traslado? {
        return _traslados.value.find { it.estadoAuditoria == EstadoAuditoria.EN_RUTA }
    }

    fun onViajeDestinoChange(destinoId: String) {
        val destino = _destinos.value.find { it.id == destinoId }
        _viajeState.update {
            it.copy(
                destinoId = destinoId,
                peajesClp = (destino?.peajeBaseClp ?: 0).toString(),
                errores = it.errores.copy(destinoId = null)
            )
        }
    }

    fun onViajeVehiculoChange(patente: String) {
        _viajeState.update {
            it.copy(vehiculoPatente = patente, errores = it.errores.copy(vehiculoPatente = null))
        }
    }

    fun onViajeOdometroInicialChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() }
        _viajeState.update {
            it.copy(odometroInicial = filtrado, errores = it.errores.copy(odometroInicial = null))
        }
    }

    fun onViajeOdometroFinalChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() }
        _viajeState.update {
            it.copy(odometroFinal = filtrado, errores = it.errores.copy(odometroFinal = null))
        }
    }

    fun onViajePeajesChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() }
        _viajeState.update {
            it.copy(peajesClp = filtrado, errores = it.errores.copy(peajesClp = null))
        }
    }

    fun onViajeJustificacionChange(valor: String) {
        _viajeState.update {
            it.copy(justificacion = valor, errores = it.errores.copy(justificacion = null))
        }
    }

    fun onViajeFotoCapturada(uriString: String) {
        _viajeState.update {
            it.copy(fotoUri = uriString, errores = it.errores.copy(fotoRespaldo = null))
        }
    }

    fun onUbicacionGpsActualizada(coordenadas: String) {
        _viajeState.update { it.copy(gpsActual = coordenadas) }
    }

    fun prepararFormularioViaje() {
        val enCurso = obtenerViajeEnCurso()
        if (enCurso != null) {
            // Preparar para finalizar el viaje activo
            val destino = _destinos.value.find { it.id == enCurso.destinoId }
            _viajeState.update {
                it.copy(
                    destinoId = enCurso.destinoId,
                    vehiculoPatente = enCurso.vehiculoPatente,
                    odometroInicial = enCurso.odometroInicial.toString(),
                    odometroFinal = "",
                    peajesClp = (destino?.peajeBaseClp ?: 0).toString(),
                    justificacion = "",
                    fotoUri = enCurso.fotoUri,
                    errores = ViajeErrores()
                )
            }
        } else {
            // Preparar para iniciar un nuevo viaje sugeriendo el último odómetro registrado
            val ultimoCerrado = _traslados.value.firstOrNull { it.odometroFinal > 0 }
            val odoSugerido = ultimoCerrado?.odometroFinal?.toString() ?: "45335"
            val destDefault = _destinos.value.firstOrNull()
            val vehDefault = _vehiculos.value.firstOrNull()
            _viajeState.update {
                it.copy(
                    destinoId = destDefault?.id ?: "",
                    vehiculoPatente = vehDefault?.patente ?: "",
                    odometroInicial = odoSugerido,
                    odometroFinal = "",
                    peajesClp = (destDefault?.peajeBaseClp ?: 0).toString(),
                    justificacion = "",
                    fotoUri = "",
                    errores = ViajeErrores()
                )
            }
        }
    }

    fun validarEIniciarViaje(): Boolean {
        val st = _viajeState.value
        val destino = _destinos.value.find { it.id == st.destinoId }
        val vehiculo = _vehiculos.value.find { it.patente == st.vehiculoPatente }
        val odoIni = st.odometroInicial.toIntOrNull()

        val errDestino = if (destino == null) "Debe seleccionar un destino activo del catastro" else null
        val errVehiculo = if (vehiculo == null) "Debe seleccionar un vehículo asignado" else null
        val errOdoIni = when {
            st.odometroInicial.isBlank() -> "El odómetro inicial es obligatorio"
            odoIni == null || odoIni <= 0 -> "El odómetro inicial debe ser un número mayor a 0 km"
            else -> null
        }

        if (errDestino != null || errVehiculo != null || errOdoIni != null) {
            _viajeState.update {
                it.copy(
                    errores = ViajeErrores(
                        destinoId = errDestino,
                        vehiculoPatente = errVehiculo,
                        odometroInicial = errOdoIni
                    )
                )
            }
            return false
        }

        val usuario = _usuarioActual.value
        val tarifaActual = _tarifas.value
        val nuevoTraslado = Traslado(
            id = "TR-${1001 + _traslados.value.size}",
            tecnicoNombre = usuario?.nombre ?: "Juan Pérez (Técnico)",
            tecnicoCorreo = usuario?.correo ?: "juan.perez@teqmed.cl",
            destinoId = destino!!.id,
            destinoNombre = destino.nombre,
            destinoCiudad = destino.ciudad,
            distanciaOficialKm = destino.distanciaOficialKm,
            vehiculoPatente = vehiculo!!.patente,
            vehiculoMarca = vehiculo.marcaModelo,
            vehiculoTipo = vehiculo.tipo,
            rendimientoAplicado = vehiculo.rendimientoKmL,
            tarifaCombustibleAplicada = tarifaActual.precioCombustibleLitro,
            reembolsoKmAplicado = if (vehiculo.tipo == "Particular") tarifaActual.reembolsoParticularKm else 0,
            fechaHoraInicio = obtenerFechaHoraActual(),
            odometroInicial = odoIni!!,
            gpsInicio = st.gpsActual,
            fotoUri = st.fotoUri,
            estadoAuditoria = EstadoAuditoria.EN_RUTA,
            sincronizado = false
        )

        val nuevaLista = listOf(nuevoTraslado) + _traslados.value
        _traslados.value = nuevaLista
        viewModelScope.launch {
            dataStore.guardarTraslados(nuevaLista)
        }
        mostrarFeedbackAnimado("¡Viaje iniciado hacia ${destino.nombre}! Ubicación GPS registrada.")
        return true
    }

    fun validarYFinalizarViaje(): Boolean {
        val enCurso = obtenerViajeEnCurso() ?: return false
        val st = _viajeState.value
        val odoFin = st.odometroFinal.toIntOrNull()
        val peajes = st.peajesClp.toIntOrNull()

        val errOdoFin = when {
            st.odometroFinal.isBlank() -> "Debe ingresar el odómetro final al terminar la visita"
            odoFin == null || odoFin <= enCurso.odometroInicial ->
                "El odómetro final debe ser mayor al inicial (${enCurso.odometroInicial} km)"
            else -> null
        }

        val errPeajes = when {
            st.peajesClp.isBlank() -> "Ingrese 0 si no hubo peajes en el tramo"
            peajes == null || peajes < 0 -> "El monto de peajes debe ser mayor o igual a $0 CLP"
            else -> null
        }

        val kmRecorridos = if (odoFin != null && odoFin > enCurso.odometroInicial) {
            odoFin - enCurso.odometroInicial
        } else 0

        val desvPorcentaje = if (kmRecorridos > 0 && enCurso.distanciaOficialKm > 0) {
            ((kmRecorridos - enCurso.distanciaOficialKm) / enCurso.distanciaOficialKm) * 100.0
        } else 0.0

        val superaUmbralDesvio = desvPorcentaje > 15.0

        val errJustificacion = if (superaUmbralDesvio && st.justificacion.trim().length < 8) {
            "Desviación detectada (+${((desvPorcentaje * 10).roundToInt() / 10.0)}%). Debe escribir una justificación válida (mín. 8 caracteres)."
        } else null

        val errFoto = if (st.fotoUri.isBlank() && enCurso.fotoUri.isBlank()) {
            "Debe capturar o adjuntar una fotografía de respaldo del odómetro o peaje"
        } else null

        if (errOdoFin != null || errPeajes != null || errJustificacion != null || errFoto != null) {
            _viajeState.update {
                it.copy(
                    errores = ViajeErrores(
                        odometroFinal = errOdoFin,
                        peajesClp = errPeajes,
                        justificacion = errJustificacion,
                        fotoRespaldo = errFoto
                    )
                )
            }
            return false
        }

        // Cálculo auditable centralizado (R.15, R.24)
        val litrosConsumidos = kmRecorridos.toDouble() / enCurso.rendimientoAplicado
        val costoCombustible = (litrosConsumidos * enCurso.tarifaCombustibleAplicada).roundToInt()
        val costoDesgaste = if (enCurso.vehiculoTipo == "Particular") {
            kmRecorridos * enCurso.reembolsoKmAplicado
        } else 0
        val costoTotal = costoCombustible + costoDesgaste + peajes!!

        val viajeCerrado = enCurso.copy(
            fechaHoraFin = obtenerFechaHoraActual(),
            odometroFinal = odoFin!!,
            peajesClp = peajes,
            gpsFin = st.gpsActual,
            fotoUri = st.fotoUri.ifBlank { enCurso.fotoUri },
            kmDeclarados = kmRecorridos,
            porcentajeDesviacion = (desvPorcentaje * 10.0).roundToInt() / 10.0,
            tieneDesviacion = superaUmbralDesvio,
            justificacionTecnico = if (st.justificacion.isNotBlank()) st.justificacion.trim() else "Tramo dentro del rango oficial.",
            costoCombustibleClp = costoCombustible,
            costoDesgasteParticularClp = costoDesgaste,
            costoTotalClp = costoTotal,
            estadoAuditoria = if (superaUmbralDesvio) EstadoAuditoria.CON_DESVIACION else EstadoAuditoria.VALIDADO,
            sincronizado = false
        )

        val nuevaLista = _traslados.value.map { if (it.id == enCurso.id) viajeCerrado else it }
        _traslados.value = nuevaLista
        viewModelScope.launch {
            dataStore.guardarTraslados(nuevaLista)
        }
        mostrarFeedbackAnimado("¡Visita cerrada! Costo calculado: $$costoTotal CLP")
        return true
    }

    fun sincronizarViajesOffline() {
        viewModelScope.launch {
            _sincronizandoOffline.value = true
            delay(1400) // Simula sincronización con animación fluida (Patrón AnimacionesEstados)
            val actualizados = _traslados.value.map {
                if (it.estadoAuditoria != EstadoAuditoria.EN_RUTA) it.copy(sincronizado = true) else it
            }
            _traslados.value = actualizados
            dataStore.guardarTraslados(actualizados)
            _sincronizandoOffline.value = false
            mostrarFeedbackAnimado("¡Todos los traslados offline fueron sincronizados con éxito!")
        }
    }

    // ========================================================================
    // PROCESO P02: MANTENCIÓN DE CATÁLOGOS Y TARIFAS - ADMIN (R.06 a R.10)
    // ========================================================================
    fun onVehiculoPatenteChange(valor: String) {
        _vehiculoFormState.update {
            it.copy(patente = valor.uppercase(), errores = it.errores.copy(patente = null))
        }
    }

    fun onVehiculoMarcaChange(valor: String) {
        _vehiculoFormState.update {
            it.copy(marcaModelo = valor, errores = it.errores.copy(marcaModelo = null))
        }
    }

    fun onVehiculoTipoChange(valor: String) {
        _vehiculoFormState.update { it.copy(tipo = valor) }
    }

    fun onVehiculoRendimientoChange(valor: String) {
        _vehiculoFormState.update {
            it.copy(rendimientoKmL = valor, errores = it.errores.copy(rendimientoKmL = null))
        }
    }

    fun onVehiculoTrabajadorChange(valor: String) {
        _vehiculoFormState.update {
            it.copy(trabajadorAsignado = valor, errores = it.errores.copy(trabajadorAsignado = null))
        }
    }

    fun validarYGuardarVehiculo(): Boolean {
        val st = _vehiculoFormState.value
        val patLimpia = st.patente.trim()
        val rend = st.rendimientoKmL.replace(",", ".").toDoubleOrNull()

        val errPatente = when {
            patLimpia.length < 6 -> "Ingrese una patente válida (ej. ABCD-12)"
            _vehiculos.value.any { it.patente.equals(patLimpia, ignoreCase = true) } ->
                "Esta patente ya se encuentra registrada en el catálogo"
            else -> null
        }
        val errMarca = if (st.marcaModelo.isBlank()) "Ingrese la marca y modelo del vehículo" else null
        val errRend = if (rend == null || rend <= 0.0 || rend > 40.0) {
            "Ingrese un rendimiento válido en km/L (entre 1 y 40)"
        } else null
        val errTrab = if (st.trabajadorAsignado.isBlank()) "Debe indicar el trabajador asignado" else null

        if (errPatente != null || errMarca != null || errRend != null || errTrab != null) {
            _vehiculoFormState.update {
                it.copy(
                    errores = VehiculoErrores(
                        patente = errPatente,
                        marcaModelo = errMarca,
                        rendimientoKmL = errRend,
                        trabajadorAsignado = errTrab
                    )
                )
            }
            return false
        }

        val nuevo = Vehiculo(
            id = "V0${_vehiculos.value.size + 1}",
            patente = patLimpia,
            marcaModelo = st.marcaModelo.trim(),
            tipo = st.tipo,
            rendimientoKmL = rend!!,
            trabajadorAsignado = st.trabajadorAsignado.trim()
        )
        val nuevaLista = _vehiculos.value + nuevo
        _vehiculos.value = nuevaLista
        _vehiculoFormState.value = VehiculoFormUiState()
        viewModelScope.launch {
            dataStore.guardarVehiculos(nuevaLista)
        }
        mostrarFeedbackAnimado("¡Vehículo ${nuevo.patente} agregado al catálogo local!")
        return true
    }

    fun onDestinoNombreChange(valor: String) {
        _destinoFormState.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null)) }
    }

    fun onDestinoCiudadChange(valor: String) {
        _destinoFormState.update { it.copy(ciudad = valor, errores = it.errores.copy(ciudad = null)) }
    }

    fun onDestinoDireccionChange(valor: String) {
        _destinoFormState.update { it.copy(direccion = valor, errores = it.errores.copy(direccion = null)) }
    }

    fun onDestinoDistanciaChange(valor: String) {
        _destinoFormState.update {
            it.copy(distanciaOficialKm = valor, errores = it.errores.copy(distanciaOficialKm = null))
        }
    }

    fun onDestinoPeajeChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() }
        _destinoFormState.update {
            it.copy(peajeBaseClp = filtrado, errores = it.errores.copy(peajeBaseClp = null))
        }
    }

    fun validarYGuardarDestino(): Boolean {
        val st = _destinoFormState.value
        val dist = st.distanciaOficialKm.replace(",", ".").toDoubleOrNull()
        val peaje = st.peajeBaseClp.toIntOrNull()

        val errNom = if (st.nombre.isBlank()) "El nombre de la clínica u hospital es obligatorio" else null
        val errCiu = if (st.ciudad.isBlank()) "La ciudad del destino es obligatoria" else null
        val errDir = if (st.direccion.isBlank()) "La dirección de ubicación es obligatoria" else null
        val errDist = if (dist == null || dist <= 0.0) "La distancia oficial debe ser mayor a 0 km" else null
        val errPeaje = if (peaje == null || peaje < 0) "El peaje base debe ser mayor o igual a $0" else null

        if (errNom != null || errCiu != null || errDir != null || errDist != null || errPeaje != null) {
            _destinoFormState.update {
                it.copy(
                    errores = DestinoErrores(
                        nombre = errNom,
                        ciudad = errCiu,
                        direccion = errDir,
                        distanciaOficialKm = errDist,
                        peajeBaseClp = errPeaje
                    )
                )
            }
            return false
        }

        val nuevo = DestinoClinica(
            id = "D0${_destinos.value.size + 1}",
            nombre = st.nombre.trim(),
            ciudad = st.ciudad.trim(),
            direccion = st.direccion.trim(),
            distanciaOficialKm = dist!!,
            peajeBaseClp = peaje!!,
            latitudRef = -36.8269,
            longitudRef = -73.0497
        )
        val nuevaLista = _destinos.value + nuevo
        _destinos.value = nuevaLista
        _destinoFormState.value = DestinoFormUiState()
        viewModelScope.launch {
            dataStore.guardarDestinos(nuevaLista)
        }
        mostrarFeedbackAnimado("¡Destino '${nuevo.nombre}' guardado con distancia oficial de ${nuevo.distanciaOficialKm} km!")
        return true
    }

    fun onTarifaCombustibleChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() }
        _tarifaFormState.update {
            it.copy(precioCombustibleLitro = filtrado, errores = it.errores.copy(precioCombustibleLitro = null))
        }
    }

    fun onTarifaReembolsoChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() }
        _tarifaFormState.update {
            it.copy(reembolsoParticularKm = filtrado, errores = it.errores.copy(reembolsoParticularKm = null))
        }
    }

    fun validarYGuardarTarifas(): Boolean {
        val st = _tarifaFormState.value
        val precio = st.precioCombustibleLitro.toIntOrNull()
        val reembolso = st.reembolsoParticularKm.toIntOrNull()

        val errPrecio = if (precio == null || precio < 500 || precio > 3500) {
            "Ingrese un precio por litro válido entre $500 y $3.500 CLP"
        } else null

        val errReemb = if (reembolso == null || reembolso < 50 || reembolso > 1000) {
            "Ingrese un factor por km válido entre $50 y $1.000 CLP"
        } else null

        if (errPrecio != null || errReemb != null) {
            _tarifaFormState.update {
                it.copy(errores = TarifaErrores(precioCombustibleLitro = errPrecio, reembolsoParticularKm = errReemb))
            }
            return false
        }

        val nuevaTarifa = ParametrosTarifa(
            precioCombustibleLitro = precio!!,
            reembolsoParticularKm = reembolso!!,
            ultimaActualizacion = "${obtenerFechaHoraActual()} (CNE Biobío)"
        )
        _tarifas.value = nuevaTarifa
        viewModelScope.launch {
            dataStore.guardarTarifas(nuevaTarifa)
        }
        mostrarFeedbackAnimado("¡Tarifa actualizada a $$precio/Litro de forma centralizada!")
        return true
    }

    // ========================================================================
    // PROCESO P04: AUDITORÍA DEL JEFE DE SERVICIO Y REPORTES (R.20 a R.24)
    // ========================================================================
    fun onFiltroClienteChange(cliente: String) {
        _filtroCliente.value = cliente
    }

    fun onFiltroVehiculoChange(patente: String) {
        _filtroVehiculo.value = patente
    }

    fun onComentarioAuditoriaChange(trasladoId: String, comentario: String) {
        _comentariosAuditoria.update { it + (trasladoId to comentario) }
        _erroresAuditoria.update { it - trasladoId }
    }

    fun auditarTraslado(trasladoId: String, aprobar: Boolean): Boolean {
        val comentario = (_comentariosAuditoria.value[trasladoId] ?: "").trim()
        if (comentario.length < 5) {
            _erroresAuditoria.update {
                it + (trasladoId to "Debe ingresar un comentario de auditoría (mín. 5 caracteres) para aprobar o rechazar.")
            }
            return false
        }

        val nuevoEstado = if (aprobar) EstadoAuditoria.APROBADO else EstadoAuditoria.RECHAZADO
        val actualizados = _traslados.value.map { t ->
            if (t.id == trasladoId) {
                t.copy(
                    estadoAuditoria = nuevoEstado,
                    comentarioAuditoria = comentario
                )
            } else t
        }
        _traslados.value = actualizados
        viewModelScope.launch {
            dataStore.guardarTraslados(actualizados)
        }
        mostrarFeedbackAnimado(
            if (aprobar) "Traslado $trasladoId APROBADO por Jefe de Servicio"
            else "Traslado $trasladoId RECHAZADO / OBSERVADO"
        )
        return true
    }

    private fun obtenerFechaHoraActual(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
