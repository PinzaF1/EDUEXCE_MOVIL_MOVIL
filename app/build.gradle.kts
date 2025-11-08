plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.zavira_movil"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.zavira_movil"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    // ✅ Fix de empaquetado (conflictos META-INF)
    packaging {
        resources {
            pickFirsts += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties"
            )
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                // (opcionales pero recomendados para evitar más choques)
                "META-INF/NOTICE", "META-INF/NOTICE.txt", "META-INF/notice.txt",
                "META-INF/LICENSE", "META-INF/LICENSE.txt", "META-INF/license.txt",
                "META-INF/ASL2.0"
            )
        }
    }
}

// ✅ Forzamos material y EXCLUIMOS Ads en TODAS las configuraciones
configurations.all {
    exclude(group = "com.google.android.gms", module = "play-services-ads")
    exclude(group = "com.google.android.gms", module = "play-services-ads-lite")
    resolutionStrategy.force("com.google.android.material:material:1.13.0-alpha05")
}

dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Material
    implementation("com.google.android.material:material:1.13.0-alpha05")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // CircleProgress (JitPack)
    implementation("com.github.lzyzsd:circleprogress:1.2.1")

}
