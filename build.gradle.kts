// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val localMavenPath: String by rootProject
    repositories {
        google()
        mavenCentral()
        maven(localMavenPath)
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    val localMavenPath: String by rootProject
    repositories {
        google()
        mavenCentral()
        maven(localMavenPath)
    }
}

//tasks.register("clean", Delete) {
//    delete rootProject.buildDir
//}