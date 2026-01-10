plugins {
    id("com.android.application")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.behave"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.behave"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth") // Add this line
    implementation("com.google.firebase:firebase-storage")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.navigation:navigation-ui:2.8.5")
}
