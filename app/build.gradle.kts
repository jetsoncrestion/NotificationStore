plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.ratna.notificationstore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ratna.notificationstore"
        minSdk = 26
        targetSdk = 34
        versionCode = 12
        versionName = "3.3"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Add this block to resolve the duplicate META-INF/DEPENDENCIES issue
    packagingOptions {
        resources.excludes.add("META-INF/DEPENDENCIES") // Exclude the conflicting file
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.car.ui.lib)
    implementation(libs.gridlayout)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation ("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("com.google.api-client:google-api-client-android:2.2.0")
    implementation ("com.google.http-client:google-http-client-gson:1.43.3")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation ("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0")
    implementation ("com.airbnb.android:lottie:6.6.2")
}