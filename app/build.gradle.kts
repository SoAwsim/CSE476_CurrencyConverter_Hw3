import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val apiKeyFile = project.rootProject.file("apikey.properties")
val apiKeyFileTemplate = project.rootProject.file("apikey.properties.template")

if (apiKeyFile.exists()) {
    println("apikey.properties already exist")
} else {
    apiKeyFile.writeText(apiKeyFileTemplate.readText())
    println("apikey.properties has been created from the apikey.properties.template")
}

android {
    namespace = "com.example.cse476.currencyconverterhw3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cse476.currencyconverterhw3"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val properties = Properties()
        properties.load(apiKeyFile.inputStream())
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_KEY", "\"${properties["CURRENCY_API_KEY"]}\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}