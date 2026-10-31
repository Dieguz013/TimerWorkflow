plugins {
    id("com.android.application")
}

android {
    namespace = "com.consoleoperativa.fasi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.consoleoperativa.fasi"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            // Sorgente unico della web app: <radice>/src/web (viene incluso come asset, index.html)
            assets.srcDir(file("../../src/web"))
        }
    }
}
