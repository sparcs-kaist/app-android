// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false

    alias(libs.plugins.hilt.gradle.plugin) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.firebase.crashlytics") version "3.0.6" apply false
    id ("org.jetbrains.kotlin.plugin.serialization") version "2.0.0" apply false
    id("com.mikepenz.aboutlibraries.plugin") version "11.2.3" apply false
}