# TeqRuta – Control y Auditoría de Traslados en Terreno (TEQMED SpA)

**Asignatura:** Desarrollo de Aplicaciones Móviles (`DSY1105`)  
**Evaluación:** Evaluación Parcial 2 – Encargo (MVP Funcional)  
**Institución:** Duoc UC – Escuela de Informática y Telecomunicaciones (Sede Concepción)  
**Integrantes:**
* Vicente Provoste
* Agustín Vásquez

---

## 1. Descripción del Proyecto y Caso de Negocio

**TeqRuta** es una aplicación móvil nativa para **Android** desarrollada en **Kotlin y Jetpack Compose** bajo arquitectura **MVVM**, diseñada para resolver la problemática de control de costos operativos de movilización de **TEQMED SpA** (empresa de servicio técnico y mantenimiento de equipamiento médico en el Gran Concepción, Chillán, Los Ángeles y Temuco).

La solución reemplaza el registro estimativo en libreta de papel por un registro digital en terreno (**Offline-First**), permitiendo:
1. Registrar el inicio y cierre de cada visita desde el celular, incluso sin conexión a internet hasta por 8 horas.
2. Capturar evidencia verificable mediante **2 recursos nativos del dispositivo**: **Geolocalización GPS** y **Cámara Fotográfica**.
3. Calcular automáticamente el costo real de cada traslado con tarifas de combustible centralizadas y auditables.
4. Detectar desviaciones automáticamente cuando el kilometraje declarado supera en más de un **15%** la distancia oficial del tramo.

---

## 2. Arquitectura Modular MVVM y Paquetes (`com.teqmed.teqruta`)

* `model/`
  * `ModelosNegocio.kt`: Entidades de dominio (`Usuario`, `RolUsuario`, `Vehiculo`, `DestinoClinica`, `ParametrosTarifa`, `Traslado`, `EstadoAuditoria`).
  * `FormulariosUiState.kt`: Clases de estado (`*UiState`) y de errores por campo (`*Errores`) desacopladas de la interfaz.
* `data/`
  * `TeqRutaDataStore.kt`: Persistencia local con **Jetpack DataStore** (`preferencesDataStore`) y datos semilla pre-cargados de la Región del Biobío.
* `viewmodel/`
  * `TeqRutaViewModel.kt`: Lógica centralizada de validación de formularios, cálculo de costos, detección de desviaciones ($>15\%$) y gestión reactiva con `StateFlow`.
* `navigation/`
  * `AppNavigation.kt`: Grafo de navegación consistente (`NavHost`) con redirección según el rol autenticado.
* `ui/components/`
  * `CampoValidado.kt`: Campo de formulario reutilizable con íconos de estado (`Error` / `CheckCircle`) y retroalimentación visual animada.
  * `MapaRadarGps.kt`: Visor de mapa y radar GPS en vivo para el conductor en terreno.
  * `ModalConfiguracionTuerca.kt`: Menú de configuración (`⚙️`) con estado de sesión, sincronización offline animada y cierre de sesión.
* `ui/screen/`
  * `LoginRegistroScreen.kt`: Inicio de sesión y registro de usuarios con selector de rol.
  * `MapaConductorScreen.kt`: Pantalla principal del conductor con mapa GPS, botón `INICIO` / `FINALIZAR` y tuerca `⚙️`.
  * `FormularioViajeScreen.kt`: Registro de apertura y cierre de traslado con captura de Cámara nativa y GPS.
  * `AdminPanelScreen.kt`: Mantenedor de vehículos de trabajadores, destinos (clínicas) y precio por litro de combustible.
  * `JefeServicioScreen.kt`: Supervisión de justificación, foto y GPS, aprobación/rechazo con comentario de auditoría y filtro de costos por cliente.
  * `FichaDetalleTrasladoScreen.kt`: Ficha completa del traslado con foto ampliada, GPS y desglose del cálculo paso a paso.

---

## 3. Cuentas Pre-cargadas para Pruebas Rápidas

| Rol en el Sistema | Correo / Usuario | Contraseña |
| :--- | :--- | :--- |
| **Técnico de Servicio (Conductor)** | `juan.perez@teqmed.cl` | `123456` |
| **Jefe de Servicio / Finanzas** | `jefe@teqmed.cl` | `123456` |
| **Administrador del Sistema** | `admin@teqmed.cl` | `123456` |

*(En la pantalla de Login también puedes tocar los botones de acceso rápido inferiores para autocompletar cualquiera de los 3 perfiles).*

---

## 4. Pasos para Ejecutar el Proyecto

1. Abrir **Android Studio** y seleccionar **Open**.
2. Elegir la carpeta del proyecto `TeqRuta`.
3. Esperar a que finalice la sincronización de **Gradle** (`Sync Project with Gradle Files`).
4. Seleccionar un emulador o dispositivo físico Android (**API 26 / Android 8.0 o superior**).
5. Presionar **Run 'app'** (`Shift + F10`).
