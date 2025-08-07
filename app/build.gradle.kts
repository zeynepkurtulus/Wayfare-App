plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.zeynekurtulus.wayfare"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zeynekurtulus.wayfare"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

    dependencies {

        // Core Android
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")

        // Architecture Components
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
        implementation("androidx.activity:activity-ktx:1.9.3")
        implementation("androidx.fragment:fragment-ktx:1.8.5")

        // Navigation
        implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
        implementation("androidx.navigation:navigation-ui-ktx:2.8.4")

        // Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

        // Networking
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

        // JSON parsing
        implementation("com.google.code.gson:gson:2.10.1")

        // Image loading
        implementation("com.github.bumptech.glide:glide:4.16.0")
        implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
        implementation("de.hdodenhof:circleimageview:3.1.0")
        implementation(libs.androidx.activity)
        kapt("com.github.bumptech.glide:compiler:4.16.0")

        // RecyclerView
        implementation("androidx.recyclerview:recyclerview:1.3.2")

        // SwipeRefreshLayout
        implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

        // Room (for local caching if needed)
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")
        kapt("androidx.room:room-compiler:2.6.1")

        // Preferences
        implementation("androidx.preference:preference-ktx:1.2.1")

        // Testing
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.mockito:mockito-core:5.1.1")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

        implementation("com.github.bumptech.glide:glide:4.16.0")
        kapt("com.github.bumptech.glide:compiler:4.16.0")
    }
}
