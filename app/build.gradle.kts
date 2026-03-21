import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.gradle.plugin)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

val properties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}
val mapKey: String = properties.getProperty("map_api_key")
val sidAuthToken: String = properties.getProperty("otl_sid_auth_token")

android {
    namespace = "org.sparcs.soap"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("buddy_appKey.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    defaultConfig {
        manifestPlaceholders += mapOf("mapKey" to mapKey)
        manifestPlaceholders += mapOf("sidAuthToken" to sidAuthToken)
        applicationId = "org.sparcs.soap"
        minSdk = 31
        targetSdk = 35
        versionCode = 8
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "sparcsapp"
        buildConfigField("String", "MAPS_API_KEY", "\"$mapKey\"")
        buildConfigField("String", "OTL_SID_AUTH_TOKEN", "\"$sidAuthToken\"")
    }

    buildTypes {

        getByName("debug") {
            manifestPlaceholders += mapOf(
                "taxiHost" to "taxi.dev.sparcs.org",
                "araHost" to "newara.dev.sparcs.org",
                "feedHost" to "buddy.dev.sparcs.org"
            )
            buildConfigField("String", "TAXI_HOST", "\"taxi.dev.sparcs.org\"")
            buildConfigField("String", "ARA_HOST", "\"newara.dev.sparcs.org\"")
        }

        getByName("release") {
            manifestPlaceholders += mapOf(
                "taxiHost" to "taxi.sparcs.org",
                "araHost" to "newara.sparcs.org",
                "feedHost" to "sparcs.org"
            )
            buildConfigField("String", "TAXI_HOST", "\"taxi.sparcs.org\"")
            buildConfigField("String", "ARA_HOST", "\"newara.sparcs.org\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.navigation.fragment)
    implementation(libs.androidx.hilt.work)
    implementation(libs.converter.gson.v290)
    implementation(libs.google.gson)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.compiler)
    implementation(libs.coil.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.security.crypto)
    implementation(libs.socket.io.client)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.osmdroid.android)
    implementation(libs.osmdroid.android)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging.ktx)

    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    debugImplementation(libs.androidx.glance.appwidget.preview)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.datastore.preferences.v111)

    implementation(libs.timber)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}