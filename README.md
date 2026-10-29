# Notaria La Legal

App Android para gestionar y seguir trámites notariales desde el celular. Un cliente crea su
trámite, adjunta el documento y lo firma; después el notario lo revisa y lo aprueba o lo devuelve
con observaciones para que el cliente lo corrija. En todo momento se puede ver en qué estado va
cada trámite y qué cambios ha tenido.

Proyecto de la asignatura Desarrollo de Aplicaciones Móviles (DSY1105), Duoc UC, basado en el
caso NotariaX. Es un prototipo: los usuarios, documentos y firmas son ficticios y la firma
electrónica solo se simula.

## Funcionalidades

**Cliente**
- Iniciar sesión o crear una cuenta.
- Crear un trámite: elegir el tipo, usar un formato predefinido o adjuntar un archivo del
  teléfono, elegir el tipo de firma y escribir los antecedentes.
- Firmar y enviar el trámite al notario.
- Ver sus trámites con el estado de cada uno.
- Corregir y reenviar un trámite devuelto con observaciones.

**Notario**
- Ver todos los trámites recibidos.
- Aprobar un trámite o devolverlo escribiendo una observación.

**Ambos**
- Historial de cada trámite con fecha, estado y responsable de cada cambio.
- La sesión queda guardada: si se cierra la app sin cerrar sesión, al abrirla se entra directo.

## Cuentas de prueba

| Perfil  | Correo                 | Contraseña |
|---------|------------------------|------------|
| Cliente | demo@lalegal.test      | Demo1234   |
| Notario | notario@lalegal.test   | Notario123 |

La app parte con dos trámites de ejemplo del cliente de prueba.

## Tecnologías

- Kotlin y Jetpack Compose (Material 3)
- Arquitectura MVVM
- Navigation Compose para moverse entre pantallas
- Room para guardar la sesión del usuario
- Coroutines y Flow para actualizar la interfaz

## Cómo ejecutarlo

1. Clonar el repositorio y abrirlo en Android Studio.
2. Esperar a que termine la sincronización de Gradle (si pide instalar el SDK de Android 37,
   aceptar).
3. Conectar un teléfono con depuración USB activada o iniciar un emulador (Android 6.0 o superior).
4. Presionar **Run**.

## Estructura

```
app/src/main/java/com/duoc/notarialalegal/
├── MainActivity.kt        Punto de entrada, crea el LoginViewModel y la navegación
├── model/                 Datos de la app: Usuario, Tramite, Historial, Sesion,
│                          base de datos Room, catálogo y validaciones
├── repository/            De dónde salen los datos (usuarios, trámites, sesión y archivos)
├── viewmodel/             Estado y lógica de cada pantalla
└── ui/                    Pantallas en Compose, navegación y tema de colores
```

La vista nunca modifica los datos directamente: cada pantalla le avisa a su ViewModel lo que hizo
el usuario, el ViewModel valida y le pide los cambios al repositorio, y la pantalla se actualiza
sola cuando cambia el estado.

Los usuarios y los trámites se mantienen en memoria con datos de prueba, así que vuelven a su
estado inicial al cerrar la app. Lo único que se guarda en el teléfono es la sesión.

## Pruebas

```sh
./gradlew :app:testDebugUnitTest          # validaciones de los formularios
./gradlew :app:connectedDebugAndroidTest  # pantallas (con un teléfono o emulador conectado)
```

Las pruebas de pantallas parten desde el login, así que hay que cerrar sesión en la app antes
de ejecutarlas.

## Equipo

- Andrés Vergara
- Rodrigo Salinas
- Josue Millahuala
