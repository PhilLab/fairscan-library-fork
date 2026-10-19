// Minimal host app for the :app library: lets you run and try FairScan on its own, and exercises
// the library's manifest/R8 setup. It deliberately has none of upstream's release logic (signing,
// ABI splits, versionCode scheme) so there is nothing here to keep in sync with upstream.
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.fairscan.sample"
    compileSdk = 37

    defaultConfig {
        // Same id as the upstream app, so a debug build keeps its data; it also means this can
        // not be installed next to the real FairScan (identical FileProvider authority).
        applicationId = "org.fairscan.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "sample"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // App-specific rules come from the :app library via consumerProguardFiles
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(project(":app"))
    // :app only depends on this as `implementation`, so it isn't exposed transitively - declared
    // directly here since this module's own manifest references androidx.core.content.FileProvider.
    implementation(libs.androidx.core.ktx)
}
