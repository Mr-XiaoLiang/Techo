plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    alias(libs.plugins.androidApplication)
//    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.lollipop.techo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lollipop.techo"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.okhttp)
    implementation(libs.glide)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.bracketsCore)

    implementation(project(path = ":palette"))
    implementation(project(path = ":gallery"))
    implementation(project(path = ":maskGuide"))
    implementation(project(path = ":base"))
    implementation(project(path = ":bigBoom"))
    implementation(project(path = ":recorder"))

//    implementation "com.lollipop.plugin:bridge:1.0"
    implementation(project(path = ":renderScript"))
    implementation(project(path = ":web"))
    implementation(project(path = ":ltabview"))
    implementation(project(path = ":qr"))
    implementation(project(path = ":clip"))
    implementation(project(path = ":colorRes"))
    implementation(project(path = ":fragmentHelper"))
    implementation(project(path = ":verticalPage"))
    implementation(project(path = ":widget"))
    implementation(project(path = ":pigment"))
    implementation(project(path = ":insets"))
    // https://juejin.cn/post/7043616310369976328
    // https://developer.android.com/guide/topics/ui/drag-drop
//    implementation "androidx.draganddrop:draganddrop:1.0.0-alpha02"
}