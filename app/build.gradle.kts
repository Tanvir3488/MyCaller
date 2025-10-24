plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bnw.voip"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bnw.voip"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"https://api.restful-api.dev/\"")
        
        // Add 16KB page size support
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
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
        debug {
            isMinifyEnabled = false
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
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }
    
    packaging {
        // Handle native libraries for 16KB page size compatibility
        jniLibs {
            useLegacyPackaging = true
            // Include specific libraries with alignment fixes
            pickFirsts += setOf(
                "**/libc++_shared.so",
                "**/liblinphone.so", 
                "**/libbctoolbox.so",
                "**/libbctoolbox-tester.so",
                "**/libZXing.so",
                "**/libjsoncpp.so",
                "**/liblinphonetester.so",
                "**/libmediastreamer.so",
                "**/libmsaaudio.so",
                "**/libmsandroidcamera2.so",
                "**/libmswebrtc.so",
                "**/libortp.so",
                "**/libsrtp2.so"
            )
        }
        
        // Exclude duplicate native libraries
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/previous-compilation-data.bin"
        }
    }
    
    // Add splits configuration for better APK compatibility
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
    
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.linphone.sdk.android)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation(libs.androidx.hilt.navigation.fragment)

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("io.coil-kt:coil:2.4.0")
}
