plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.homescreen"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.homescreen"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
   vectorDrawables.useSupportLibrary= true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    //UNIT TEST DEPENDENCIES
    testImplementation(libs.junit)
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.robolectric:robolectric:4.7.3")  // Ensure you have the correct version
    testImplementation ("org.mockito:mockito-core:3.11.2")
    testImplementation ("androidx.test:core:1.3.0")
    testImplementation ("androidx.test.ext:junit:1.1.2")
    testImplementation ("androidx.test.espresso:espresso-core:3.3.0")

    //INSTRUMENTED TEST DEPENDENCIES
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("org.mockito:mockito-android:4.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
}