# Notaria La Legal

App Android para gestionar y seguir trámites notariales desde el celular. Un cliente crea su
trámite, adjunta el documento (o le saca una foto) y lo firma; después el notario lo revisa y lo
aprueba o lo devuelve con observaciones para que el cliente lo corrija. En todo momento se puede
ver en qué estado va cada trámite y qué cambios ha tenido.

Proyecto de la asignatura Desarrollo de Aplicaciones Móviles (DSY1105), Duoc UC, basado en el
caso NotariaX.

## Integrantes

- Andrés Vergara
- Rodrigo Salinas
- Josue Millahuala

## Funcionalidades

**Cliente**
- Crear una cuenta e iniciar sesión.
- Crear un trámite: elegir el tipo, usar un formato predefinido o adjuntar un documento
  (desde los archivos del teléfono o tomando una foto con la cámara), elegir el tipo de firma y
  escribir los antecedentes.
- Firmar y enviar el trámite al notario.
- Ver sus trámites con el estado de cada uno.
- Corregir y reenviar un trámite devuelto con observaciones.
- Eliminar un trámite que todavía no está aprobado.

**Notario**
- Ver todos los trámites recibidos.
- Aprobar un trámite o devolverlo escribiendo una observación.

**Ambos**
- Historial de cada trámite con fecha, estado y responsable de cada cambio.
- Abrir el archivo adjunto desde el detalle con **Ver documento**. La app guarda una copia
  privada del archivo en el teléfono y la abre con un visor instalado.
- La sesión queda guardada con Room: si se cierra la app sin cerrar sesión, al abrirla se entra
  directo.

**Recursos nativos**
- Selector de archivos del sistema para adjuntar documentos.
- Cámara para fotografiar el documento.

## Endpoints utilizados

Los usuarios, los trámites y su historial se guardan en una API creada en
[Xano](https://www.xano.com). La app la consume con Retrofit (`remote/ApiService.kt`).

URL base: `https://x8ki-letl-twmt.n7.xano.io/api:notaria/`

| Método | Endpoint                 | Uso en la app                                   |
|--------|--------------------------|-------------------------------------------------|
| POST   | `/auth/signup`           | Crear una cuenta                                |
| POST   | `/auth/login`            | Iniciar sesión (devuelve el token)              |
| GET    | `/auth/me`               | Obtener el nombre y el rol del usuario          |
| GET    | `/tramite`               | Listar los trámites                             |
| GET    | `/tramite/{tramite_id}`  | Ver el detalle de un trámite                    |
| POST   | `/tramite`               | Crear un trámite firmado                        |
| PATCH  | `/tramite/{tramite_id}`  | Aprobar, devolver o reenviar un trámite         |
| DELETE | `/tramite/{tramite_id}`  | Eliminar un trámite                             |
| GET    | `/historial`             | Ver los cambios de un trámite                   |
| POST   | `/historial`             | Registrar cada cambio de estado                 |

## Cuentas de prueba

| Perfil  | Correo                 | Contraseña |
|---------|------------------------|------------|
| Cliente | demo@lalegal.test      | Demo1234   |
| Notario | notario@lalegal.test   | Notario123 |

Las cuentas nuevas se crean como **Cliente**. El rol **Notario** se asigna desde la tabla `user`
en Xano.

## Tecnologías

- Kotlin y Jetpack Compose (Material 3)
- Arquitectura MVVM
- Navigation Compose para moverse entre pantallas
- Retrofit y Gson para consumir la API de Xano
- Room para guardar la sesión del usuario
- Coroutines para las llamadas a la API sin bloquear la pantalla
- JUnit y kotlinx-coroutines-test para las pruebas unitarias

## Cómo ejecutarlo

1. Clonar el repositorio y abrirlo en Android Studio.
2. Esperar a que termine la sincronización de Gradle (si pide instalar el SDK de Android 37,
   aceptar).
3. Conectar un teléfono con depuración USB activada o iniciar un emulador (Android 6.0 o
   superior) con conexión a internet.
4. Presionar **Run**.

Para instalar directamente el APK firmado, copiar `apk/NotariaLaLegal.apk` al teléfono y abrirlo
(hay que permitir la instalación de apps de origen desconocido).

## APK firmado

El APK de release se firma con la llave `app/notaria-la-legal.jks`. Los datos de la llave están en
`keystore.properties` y la configuración de firma en `app/build.gradle.kts` (`signingConfigs`).

Para generarlo de nuevo:

```sh
./gradlew :app:assembleRelease
```

El archivo queda en `app/build/outputs/apk/release/app-release.apk`. La versión entregada está en
`apk/NotariaLaLegal.apk`.

![APK firmado y llave](docs/apk_firmado.png)

## Pruebas

```sh
./gradlew :app:testDebugUnitTest          # pruebas unitarias (no necesitan internet)
./gradlew :app:connectedDebugAndroidTest  # pantallas (con un teléfono o emulador conectado)
```

Las pruebas unitarias están en `app/src/test` y revisan las validaciones de los formularios y los
repositorios de usuarios y trámites. Para no depender de internet, los repositorios se prueban con
`ApiFalsa`, una implementación de `ApiService` que guarda los datos en listas.

Las pruebas de pantallas parten desde el login, así que hay que cerrar sesión en la app antes de
ejecutarlas.

## Estructura

```
app/src/main/java/com/duoc/notarialalegal/
├── MainActivity.kt        Punto de entrada, crea el LoginViewModel y la navegación
├── model/                 Datos de la app: Usuario, Tramite, Historial, Sesion,
│                          base de datos Room, catálogo y validaciones
├── remote/                Conexión con Xano: ApiService, RetrofitInstance y datos de la API
├── repository/            De dónde salen los datos (usuarios, trámites, sesión y archivos)
├── viewmodel/             Estado y lógica de cada pantalla
└── ui/                    Todas las pantallas, componentes y navegación
    └── theme/             Colores, tipografía y tema compartido
```

La vista nunca modifica los datos directamente: cada pantalla le avisa a su ViewModel lo que hizo
el usuario, el ViewModel valida y le pide los cambios al repositorio, el repositorio llama a la
API y la pantalla se actualiza cuando cambia el estado.

## Video

[Ver el funcionamiento de la app (entrega 1)](https://youtu.be/Emt4_S9r3Lk)
