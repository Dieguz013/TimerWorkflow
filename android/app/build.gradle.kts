plugins {
    id("com.android.application")
}

android {
    namespace = "com.consoleoperativa.fasi"
    // 36 = API delle notifiche "Live" (Android 16); targetSdk resta 34 per non cambiare il layout della WebView
    compileSdk = 36

    defaultConfig {
        applicationId = "com.consoleoperativa.fasi"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        encoding = "UTF-8"
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
