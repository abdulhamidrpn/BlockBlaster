import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

val localProperties = Properties()
val localPropertiesFile: File = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
fun getSecret(name: String, default: String = ""): String {
    return localProperties.getProperty(name, System.getenv(name) ?: default)
}
android {
    namespace = "com.rpn.blockblaster"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        applicationId = "com.rpn.blockblaster"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        manifestPlaceholders["playGamesAppId"] = getSecret("PLAY_GAMES_APP_ID", "")
        
        buildConfigField("String", "ADMOB_BANNER_ID", "\"${getSecret("ADMOB_BANNER_ID", "ca-app-pub-3940256099942544/9214589741")}\"")
        buildConfigField("String", "ADMOB_REWARD_ID", "\"${getSecret("ADMOB_REWARD_ID", "ca-app-pub-3940256099942544/5224354917")}\"")
        buildConfigField("String", "PLAY_GAMES_LEADERBOARD_ID", "\"${getSecret("PLAY_GAMES_LEADERBOARD_ID", "")}\"")
        buildConfigField("String", "PLAY_GAMES_ACHIEVEMENT_ID", "\"${getSecret("PLAY_GAMES_ACHIEVEMENT_ID", "")}\"")
    }

    signingConfigs {
        create("release") {
            // Provide the path to your .jks file relative to the app directory
            storeFile = rootProject.file("keystore")
            storePassword = getSecret("KEYSTORE_PASSWORD")
            keyAlias = getSecret("KEY_ALIAS")
            keyPassword = getSecret("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["admobAppId"] = getSecret("ADMOB_APP_ID", "")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.androidx.material.icons.extended)
    implementation(libs.accompanist.permissions)
    implementation(libs.timber)

    implementation(libs.google.fonts)

    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.ktor)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    implementation(libs.bundles.datastore)
    implementation(libs.bundles.koin)
    api(libs.koin.core)


    implementation("com.google.android.gms:play-services-ads:25.1.0")
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    
    // Play Ecosystem
    implementation(libs.bundles.play.services)
}
