# Notaria La Legal

Aplicación Android del proyecto DSY1105, basada en el caso NotariaX. Permite que un
cliente envíe una solicitud y que un notario la revise, la apruebe o la devuelva
con observaciones.

## Qué se puede hacer

- Registrarse e iniciar sesión.
- Crear un trámite con sus antecedentes y el tipo de firma.
- Adjuntar un PDF, una imagen o tomar una fotografía con la cámara.
- Usar un formato de solicitud generado en PDF.
- Enviar el trámite y consultar su estado e historial.
- Descargar y abrir el documento desde el detalle.
- Como notario, aprobar o devolver con una observación.
- Como cliente, adjuntar una corrección y reenviarla.
- Mantener la sesión al cerrar y volver a abrir la aplicación.

La firma es una representación para el proyecto académico. No corresponde a una
firma electrónica certificada ni convierte la solicitud en un documento notarial válido.

## Tecnologías

Kotlin, Jetpack Compose y Material 3 para las pantallas; Navigation Compose para
la navegación; Retrofit y Gson para los llamados HTTP; Xano como backend; Room
para la sesión local; coroutines y Flow para actualizar el estado de las vistas.

La aplicación usa MVVM. El recorrido de una acción es:

`Pantalla → ViewModel → Repository → API de Xano`

Cuando llega la respuesta, el repositorio actualiza los datos y el ViewModel
permite que Compose muestre el resultado. Los llamados HTTP no se hacen desde las vistas.

## Estructura del proyecto

Los archivos de Kotlin están en `app/src/main/java/com/duoc/notarialalegal/`.

| Carpeta o archivo | Responsabilidad |
|---|---|
| `MainActivity.kt` | Inicia la aplicación, prepara los repositorios y muestra la navegación. |
| `model/` | Clases Usuario, Tramite, Historial y DocumentoAdjunto; catálogo de opciones y validaciones. |
| `model/AppDatabase.kt`, `Sesion.kt`, `SesionDao.kt` | Base de datos, tabla y consultas de la sesión en Room. |
| `network/ClienteXano.kt` | URL del servidor, Retrofit y envío del token. |
| `network/XanoApi.kt` | Métodos y rutas de los endpoints. |
| `network/ModelosApi.kt` | Datos que se envían y reciben en JSON. |
| `network/ErroresApi.kt` | Mensajes para problemas de conexión o respuestas HTTP con error. |
| `repository/UsuarioRepository.kt` | Registro, login y consulta del usuario autenticado. |
| `repository/TramiteRepository.kt` | Consulta y cambios de trámites, historial y descarga del documento. |
| `repository/ArchivoRepository.kt` | Copias privadas, fotos, PDF de solicitud y conversión de adjuntos a Base64. |
| `repository/SesionRepository.kt` | Guarda y recupera la sesión desde Room. |
| `repository/TokenSeguro.kt`, `SesionActual.kt` | Cifrado del token y sesión actual en memoria. |
| `viewmodel/` | Estado de los formularios, validaciones, carga, errores y acciones de cada pantalla. |
| `ui/` | Login, registro, inicio, nuevo trámite, detalle y componentes compartidos. |
| `ui/Navegacion.kt` | Rutas y cambios entre pantallas. |
| `ui/SelectorDocumento.kt` | Abre el selector de archivos o la cámara de Android. |
| `ui/theme/` | Paleta azul y blanca, tema y tipografía. |

Los usuarios, trámites, estados e historial se guardan en Xano. No hay una lista
local de usuarios ni trámites precargados. Las listas de tipos de trámite y firma
son opciones fijas del formulario, no registros de usuarios o solicitudes.

Room guarda una única sesión: usuario, correo, rol e identificador, junto con el
token cifrado. La clave de cifrado queda en Android Keystore y la contraseña no se
guarda en el teléfono. Al cerrar sesión se borra la sesión local y la lista temporal.

## APIs

Servidor: `https://x8ki-letl-twmt.n7.xano.io/`.

Hay 11 endpoints distintos: 5 GET y 6 POST. Salvo registro e ingreso, requieren
`Authorization: Bearer <token>`. El servidor controla el usuario, el rol, el
propietario del trámite y los cambios de estado permitidos.

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api:T9E8dGD9/auth/signup` | Registrar una cuenta Cliente. |
| POST | `/api:T9E8dGD9/auth/login` | Iniciar sesión y obtener el token. |
| GET | `/api:T9E8dGD9/auth/me` | Consultar al usuario de la sesión. |
| GET | `/api:notaria-la-legal/tramites` | Listar los trámites que puede ver la cuenta. |
| POST | `/api:notaria-la-legal/tramites` | Crear un trámite y su primer movimiento. |
| GET | `/api:notaria-la-legal/tramites/{id}` | Consultar el detalle. |
| GET | `/api:notaria-la-legal/tramites/{id}/historial` | Consultar los movimientos. |
| GET | `/api:notaria-la-legal/tramites/{id}/documento` | Descargar el adjunto con permiso de acceso. |
| POST | `/api:notaria-la-legal/tramites/{id}/aprobar` | Aprobar como Notario. |
| POST | `/api:notaria-la-legal/tramites/{id}/devolver` | Devolver con observaciones como Notario. |
| POST | `/api:notaria-la-legal/tramites/{id}/reenviar` | Corregir y reenviar como Cliente. |

El registro envía `name`, `email` y `password`. El login envía `email` y `password`
y recibe `authToken` y `user`. Una cuenta creada desde el formulario siempre es
Cliente; el permiso Notario se asigna desde la administración de Xano.

Crear un trámite envía `tipo`, `documento`, `tipo_firma`, `antecedentes`,
`documento_base64` y `documento_mime`. Para corregirlo se envían el nuevo nombre,
el archivo y un comentario opcional. El servidor obtiene al propietario desde
el token; la app no puede elegir otro propietario ni enviarse un rol Notario.

Los estados son `Enviado al notario`, `Con observaciones` y `Aprobado`. Cada
cambio agrega un registro al historial. Se muestra un error cuando no hay Internet,
la sesión no es válida o la cuenta no tiene permiso. No se repiten automáticamente
las operaciones de creación, aprobación o reenvío.

El plan gratuito de Xano limita las solicitudes a 10 cada 20 segundos. Si se llega
al límite, hay que esperar y volver a intentar. No es necesario pagar para probar
este proyecto.

## Documentos y recursos nativos

Se usan dos recursos del teléfono:

1. **Archivos:** selector del sistema para elegir PDF, JPG o PNG.
2. **Cámara:** aplicación de cámara del sistema para fotografiar el documento.

Los archivos tienen un máximo de 2 MB. Las fotografías se reducen antes de
adjuntarse. Para el respaldo, el archivo se convierte a Base64 y se guarda en Xano.
La lista y el detalle no incluyen ese contenido: se descarga por su endpoint
cuando se toca **Ver documento**. Así también puede abrirlo el notario desde
otro dispositivo. Base64 es una codificación, no un cifrado.

Android abre una copia privada con `FileProvider` y un permiso temporal de lectura.
Su declaración está en `AndroidManifest.xml` y las carpetas permitidas están en
`res/xml/documentos_paths.xml`. Hace falta un visor de PDF o imágenes instalado.
No se solicitan permisos generales de almacenamiento ni se usa una cámara propia:
la foto la toma la aplicación de cámara instalada.

Los trámites creados antes de incorporar el respaldo pueden no tener archivo en
el servidor. Para comprobar la descarga, crear un trámite nuevo con un adjunto.
Esta solución de archivos pequeños está pensada para la entrega académica; para
archivos grandes se debería usar almacenamiento especializado.

## Cómo ejecutar

1. Clonar el repositorio y abrirlo en Android Studio.
2. Sincronizar Gradle e instalar el SDK 37 si Android Studio lo solicita.
3. Usar el JDK incluido en Android Studio. El mínimo del dispositivo es Android 6.0 (API 23).
4. Iniciar un emulador o conectar un teléfono con depuración USB.
5. Presionar **Run**. Se necesita conexión a Internet para las funciones del servidor.
6. Crear una cuenta Cliente o usar las cuentas de demostración compartidas con el equipo.

Las contraseñas y tokens de las cuentas no se incluyen en el repositorio.

## Pruebas

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:lintRelease
```

Hay ocho pruebas unitarias: validación del correo y la contraseña, contrato HTTP
del login, creación de un trámite, historial, descarga del adjunto, envío de una
corrección y respuesta al límite de solicitudes. Las pruebas HTTP usan
MockWebServer, por lo que no necesitan cuentas ni Internet.

Las pruebas de pantallas verifican errores en login y registro con un dispositivo
conectado. Antes de ejecutarlas hay que estar en el login con la sesión cerrada.
Estas pruebas no sustituyen la revisión del flujo con Cliente y Notario.

Para revisar el flujo: Cliente crea una solicitud con archivo o foto → Notario
abre el documento y devuelve con observaciones → Cliente adjunta la corrección y
reenvía → Notario aprueba → ambos revisan el historial y vuelven a entrar a la app.

## APK release firmado

El APK para entregar es la variante **release**, con el ícono NL personalizado.
La clave de firma y sus contraseñas se conservan fuera de Git. Hay que guardar una
copia de esa clave para poder firmar futuras actualizaciones de la misma aplicación.

Para compilar nuevamente:

1. Copiar `firma.properties.example` como `firma.properties` en la raíz del proyecto.
2. Completar la ruta de la clave, el alias y las contraseñas locales del equipo.
3. Ejecutar `./gradlew :app:assembleRelease`.
4. El APK queda en `app/build/outputs/apk/release/app-release.apk`.

Sin el archivo de firma, Android Studio permite ejecutar debug. Para la entrega
hay que configurar la firma release; no se debe enviar un APK sin firmar.

## Trabajo del equipo

- [Repositorio GitHub](https://github.com/punished-dog/Notaria-La-Legal)
- [Tablero Trello](https://trello.com/b/dSX8rFO6/notaria-la-legal)
- [Video de funcionamiento de la entrega anterior](https://youtu.be/Emt4_S9r3Lk)

El tablero organiza las tareas y sus avances. Los cambios de código se registran
en Git con mensajes que describen su propósito. Para esta evaluación se entrega
el APK firmado; el código y la documentación quedan en el repositorio.

Equipo: Andrés Vergara, Rodrigo Salinas y Josue Millahuala.
