plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val releaseKeystorePath = System.getenv("MINHA_BATERIA_KEYSTORE_PATH")
val releaseKeystorePassword = System.getenv("MINHA_BATERIA_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("MINHA_BATERIA_KEY_ALIAS")
val releaseKeyPassword = System.getenv("MINHA_BATERIA_KEY_PASSWORD")

android {
    namespace = "com.minhabateria.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.minhabateria.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "1.0.10"
    }

    signingConfigs {
        create("release") {
            if (!releaseKeystorePath.isNullOrBlank()) {
                storeFile = file(releaseKeystorePath)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
