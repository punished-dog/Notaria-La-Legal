# Notaria La Legal

App Android para gestionar y seguir trámites notariales desde el celular. Un cliente crea su
trámite, adjunta el documento y lo firma; después el notario lo revisa y lo aprueba o lo devuelve
con observaciones para que el cliente lo corrija. En todo momento se puede ver en qué estado va
cada trámite y qué cambios ha tenido.

Proyecto de la asignatura Desarrollo de Aplicaciones Móviles (DSY1105), Duoc UC, basado en el
caso NotariaX.

## Funcionalidades

**Cliente**
- Iniciar sesión o crear una cuenta.
- Crear un trámite: elegir el tipo, usar un formato predefinido o adjuntar un documento (desde
  los archivos del teléfono o sacándole una foto), elegir el tipo de firma y escribir los
  antecedentes.
- Firmar y enviar el trámite al notario.
- Ver sus trámites con el estado de cada uno.
- Corregir y reenviar un trámite devuelto con observaciones.
- Eliminar un trámite que todavía no está aprobado.

**Notario**
- Ver todos los trámites recibidos.
- Aprobar un trámite o devolverlo escribiendo una observación.

**Ambos**
- Historial de cada trámite con fecha, estado y responsable de cada cambio.
- Abrir el archivo adjunto desde el detalle con **Ver documento**.
- La sesión queda guardada: si se cierra la app sin cerrar sesión, al abrirla se entra directo.

## Cuentas de prueba

| Perfil  | Correo                 | Contraseña |
|---------|------------------------|------------|
| Cliente | demo@lalegal.test      | Demo1234   |
| Notario | notario@lalegal.test   | Notario123 |

Las cuentas nuevas se crean como cliente. El rol de notario se cambia en la tabla `user` de Xano.

## API

Los usuarios, los trámites y el historial se guardan en una API hecha en Xano:
`https://x8ki-letl-twmt.n7.xano.io/api:notaria/`

| Método | Endpoint                | Para qué se usa                       |
|--------|-------------------------|---------------------------------------|
| POST   | `auth/signup`           | Crear una cuenta                      |
| POST   | `auth/login`            | Iniciar sesión                        |
| GET    | `auth/me`               | Datos y rol del usuario conectado     |
| GET    | `tramite`               | Listar los trámites                   |
| GET    | `tramite/{tramite_id}`  | Ver un trámite                        |
| POST   | `tramite`               | Crear un trámite                      |
| PATCH  | `tramite/{tramite_id}`  | Aprobar, devolver o reenviar          |
| DELETE | `tramite/{tramite_id}`  | Eliminar un trámite                   |
| GET    | `historial`             | Ver los cambios de un trámite         |
| POST   | `historial`             | Guardar un cambio de estado           |

## Tecnologías

- Kotlin y Jetpack Compose (Material 3)
- Arquitectura MVVM
- Navigation Compose para moverse entre pantallas
- Retrofit para conectarse con la API
- Room para guardar la sesión del usuario
- Coroutines para no bloquear la pantalla mientras responde la API
- Cámara y selector de archivos del teléfono

## Cómo ejecutarlo

1. Clonar el repositorio y abrirlo en Android Studio.
2. Esperar a que termine la sincronización de Gradle (si pide instalar el SDK de Android 37,
   aceptar).
3. Conectar un teléfono con depuración USB activada o iniciar un emulador (Android 6.0 o superior)
   con internet.
4. Presionar **Run**.

También se puede instalar directamente el APK que está en `apk/NotariaLaLegal.apk`.

## APK firmado

El APK se firma con la llave `app/notaria-la-legal.jks`. Sus datos están en `keystore.properties`
y la firma se configura en `app/build.gradle.kts`. Para generarlo:

```sh
./gradlew :app:assembleRelease
```

![APK firmado](docs/apk_firmado.png)

## Estructura

```
app/src/main/java/com/duoc/notarialalegal/
├── MainActivity.kt        Punto de entrada, crea el LoginViewModel y la navegación
├── model/                 Datos de la app: Usuario, Tramite, Historial, Sesion,
│                          base de datos Room, catálogo y validaciones
├── remote/                Conexión con la API de Xano (Retrofit)
├── repository/            De dónde salen los datos (usuarios, trámites, sesión y archivos)
├── viewmodel/             Estado y lógica de cada pantalla
└── ui/                    Todas las pantallas, componentes y navegación
    └── theme/             Colores, tipografía y tema compartido
```

La vista nunca modifica los datos directamente: cada pantalla le avisa a su ViewModel lo que hizo
el usuario, el ViewModel valida y le pide los cambios al repositorio, y el repositorio se
comunica con la API.

## Pruebas

```sh
./gradlew :app:testDebugUnitTest          # pruebas unitarias
./gradlew :app:connectedDebugAndroidTest  # pantallas (con un teléfono o emulador conectado)
```

Las pruebas unitarias revisan las validaciones y los repositorios de usuarios y trámites, usando
una API de prueba para no depender de internet. Las pruebas de pantallas parten desde el login,
así que hay que cerrar sesión en la app antes de ejecutarlas.

## Equipo

- Andrés Vergara
- Rodrigo Salinas
- Josue Millahuala

## Video

[Ver el funcionamiento de la app](https://youtu.be/Emt4_S9r3Lk)
