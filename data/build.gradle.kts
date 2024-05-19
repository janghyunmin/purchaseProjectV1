
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    flavorDimensions += "version"
    productFlavors {
        register("dev") {
            buildConfigField("String", "PIECE_API_V3", "\"https://fdev-gateway.piece.la/v3/\"")
            buildConfigField("String", "PIECE_WS_PORTFOLIO", "\"wss://fdev-websocket.piece.la/portfolio\"")
            buildConfigField("String", "PIECE_WS_PORTFOLIO_DETAIL", "\"wss://fdev-websocket.piece.la/portfolio/\"")
            buildConfigField("String", "PIECE_WEB_PAGE_LINK", "\"http://fdev.piece.la:5503/\"")
        }
        register("stage") {
            buildConfigField("String", "PIECE_API_V3", "\"https://sapgateway.piece.la/v3/\"")
            buildConfigField("String", "PIECE_WS_PORTFOLIO", "\"wss://sapwsocket.piece.la/portfolio\"")
            buildConfigField("String", "PIECE_WS_PORTFOLIO_DETAIL", "\"wss://sapwsocket.piece.la/portfolio/\"")
            buildConfigField("String", "PIECE_WEB_PAGE_LINK", "\"http://fdev.piece.la:5503/\"")
        }
        register("real") {
            buildConfigField("String", "PIECE_API_V3", "\"https://gateway.piece.run/v3/\"")
            buildConfigField("String", "PIECE_WS_PORTFOLIO", "\"wss://v2-websocket.piece.run/portfolio\"")
            buildConfigField("String", "PIECE_WS_PORTFOLIO_DETAIL", "\"wss://v2-websocket.piece.run/portfolio/\"")
            buildConfigField("String", "PIECE_WEB_PAGE_LINK", "\"https://piece.run/\"")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // (Required) Writing and executing Unit Tests on the JUnit Platform
    testImplementation(Test.JUNIT_5_API)
    testRuntimeOnly(Test.JUNIT_5_ENGINE)

    // (Optional) If you need "Parameterized Tests"
    testImplementation(Test.JUNIT_5_PARAMS)

    // (Optional) If you also have JUnit 4-based tests
    testImplementation(Test.JUNIT_4)
    testRuntimeOnly(Test.JUNIT_5_VINTAGE)

    testImplementation(Test.MOCKK)

    testImplementation(Test.TEST_KOTLIN)

    implementation ("javax.inject:javax.inject:1")

    implementation(project(":domain"))

    implementation(AndroidX.CORE)
    implementation(AndroidX.WORK)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL)
    implementation(AndroidX.LIFECYCLE_LIVEDATA)
    implementation(AndroidX.CORE)

    implementation(OkHttp.OKHTTP_3)
    implementation(OkHttp.OKHTTP_3_URLCONNECTION)
    implementation(OkHttp.OKHTTP_3_INTERCEPTOR)

    implementation(Retrofit.RETROFIT_2_ADAPTER)
    implementation(Retrofit.RETROFIT_2_GSON)

    implementation(Kotlin.COROUTINES_CORE)
    implementation(Kotlin.COROUTINES)

    implementation(Dagger.HILT)
    kapt(Dagger.COMPILER)

    implementation(Room.RUNTIME)
    kapt(Room.COMPILER)

    implementation(SQLITE.SQLITE)
    implementation(SQLITE.SQLITE_CIPHER)

    implementation(Google.GSON)

    implementation(Jetpack.PAGING)

    coreLibraryDesugaring(Android.DESUGARING)

    implementation(DataStore.DATA_STORE)
    implementation(DataStore.DATA_STORE_CORE)
    implementation(AndroidX.BIOMETRIC)
    implementation(AndroidX.BIOMETRIC_KTX)
}