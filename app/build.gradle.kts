import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "uth.cgyv.grupo.cuatro.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "uth.cgyv.grupo.cuatro.myapplication"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17) // Usa "17" si arriba usaste VERSION_17
        }
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-core:1.4.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-camera2:1.4.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-view:1.4.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-view:1.4.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-extensions:1.4.2")
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.face.detection)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
