import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Los datos de la llave para firmar el APK están en keystore.properties (raíz del proyecto)
val propiedadesFirma = Properties()
val archivoFirma = rootProject.file("keystore.properties")
if (archivoFirma.exists()) {
    archivoFirma.inputStream().use { propiedadesFirma.load(it) }
}

android {
    namespace = "com.duoc.notarialalegal"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.duoc.notarialalegal"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(propiedadesFirma.getProperty("storeFile", "notaria-la-legal.jks"))
            storePassword = propiedadesFirma.getProperty("storePassword")
            keyAlias = propiedadesFirma.getProperty("keyAlias")
            keyPassword = propiedadesFirma.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    // Room para guardar la sesión en el teléfono
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Retrofit para consumir la API de Xano
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Pruebas unitarias de repositorios y validaciones
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}