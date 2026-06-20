plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.crashlytics)
}

fun calculateVersionCode(versionName: String): Int {
    val (major, minor, patch) = versionName.split(".")
        .map { it.toIntOrNull() ?: 0 }
    return major * 10000 + minor * 100 + patch
}

android {
    namespace = "com.app.binged"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.binged"
        minSdk = 29
        targetSdk = 35

        val version = "0.1.1"
        versionName = version
        versionCode = calculateVersionCode(version)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    applicationVariants.all {
        outputs.all {
            val appName = "Binged"
            val buildType = buildType.name
            val versionName = versionName
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "${appName}-${buildType}-${versionName}.apk"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/keystore/binged-keystore.jks")
            storePassword = "binged123"
            keyAlias = "binged-key"
            keyPassword = "binged123"
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.auth.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":feature:diary"))
    implementation(project(":feature:search"))
    implementation(project(":feature:shows"))
    implementation(project(":feature:tracking"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:statistics"))
    implementation(project(":feature:tutorial"))
}
