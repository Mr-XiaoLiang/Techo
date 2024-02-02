plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lollipop.lqrdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "liang.lollipop.lqrdemo"
        minSdk = 27
        targetSdk = 34
        versionCode = 2_07_02
        versionName = "2.7.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.glide)
    implementation(project(path = ":clip"))
    implementation(project(path = ":base"))
    implementation(project(path = ":fileChooser"))
    implementation(project(path = ":qr"))
    implementation(project(path = ":colorRes"))
    implementation(project(path = ":widget"))
    implementation(project(path = ":pigment"))
    implementation(project(path = ":privacy"))
    implementation(project(path = ":faceIcon"))
    implementation(project(path = ":palette"))
    implementation(project(path = ":fragmentHelper"))
}