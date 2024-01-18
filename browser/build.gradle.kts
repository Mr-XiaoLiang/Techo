plugins {
    id( "com.android.application"      )
    id( "org.jetbrains.kotlin.android" )
    id( "kotlin-kapt"                  )
}

android {
    namespace = "com.lollipop.browser"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lollipop.browser"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "${projectDir}/schemas")
            }
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(libs.androidx.core.ktx                         )
    implementation(libs.androidx.appcompat                        )
    implementation(libs.material                                  )
    implementation(libs.androidx.constraintlayout                 )
    implementation(libs.androidx.swiperefreshlayout.v120alpha01   )
    implementation(libs.glide                                     )
    implementation(project(path = ":base")                         )
    implementation(project(path = ":web")                          )
    implementation(project(path = ":maskGuide")                    )
    implementation(project(path = ":clip")                         )
    implementation(project(path = ":fragmentHelper")               )
    implementation(project(path = ":colorRes")                     )
    implementation(project(path = ":verticalPage")                 )
    implementation(project(path = ":stitch")                       )
    implementation(project(path = ":fileChooser")                  )
}