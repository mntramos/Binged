plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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

        val version = "1.1.0"
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

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":feature:diary"))
    implementation(project(":feature:search"))
    implementation(project(":feature:shows"))
    implementation(project(":feature:tracking"))
    implementation(project(":navigation"))

    // Koin dependencies
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Jetpack Compose
    implementation(libs.androidx.navigation.compose)
}
