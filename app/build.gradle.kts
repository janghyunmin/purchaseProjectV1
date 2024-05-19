
plugins {
    kotlin("android")
    kotlin("kapt")

    id ("com.android.application")
    id ("com.google.gms.google-services")
    id ("androidx.navigation.safeargs.kotlin")
    id ("dagger.hilt.android.plugin")

    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.firebase.appdistribution")

    id("de.mannodermaus.android-junit5")
}

android {
    signingConfigs {
        create("release") {
            storePassword = "782152f6f9c34f03bb581171b794d9e8"
            keyAlias = "QHRva3Rva2hhbi5kZXYvcGllY2U="
            keyPassword = "fd2de8857d1343c8853469702b2349ce"
            storeFile = file("../piece.jks")
        }
    }

    compileSdkVersion(Apps.compileSdk)
    buildToolsVersion = Apps.buildTools
    defaultConfig {
        applicationId = Apps.APP_NAME
        minSdkVersion(Apps.minSdk)
        targetSdkVersion(Apps.targetSdk)
        versionCode = 80
        versionName = "2.2.2"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = Apps.ANDROID_JUNIT_RUNNER
        externalNativeBuild {
            cmake {
                arguments ("-DANDROID_STL=c++_static") //, "-DANDROID_TOOLCHAIN=clang"
                cppFlags ("-std=c++11 -fexceptions")
            }
        }
    }

    buildTypes {
        all {
            isCrunchPngs = false
        }
        getByName("debug") {
            isDebuggable = true
            isShrinkResources = false
            isMinifyEnabled = false
            multiDexEnabled = false
            signingConfig = signingConfigs.getByName("debug")

            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "DEBUG_VALUE", "false")

        }
        getByName("release") {
            firebaseAppDistribution {
                artifactType = "AAB"
                releaseNotesFile = "../fastlane/release-notes.txt"
            }
            isDebuggable = false // 디버깅
            isShrinkResources = true // 사용하지 않는 리소스 제거
            isMinifyEnabled = true // 사용하지 않는 코드 제거 및 코드 난독화
            signingConfig = signingConfigs.getByName("release")
            multiDexEnabled = true

            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "DEBUG_VALUE", "false")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("dev") {
            dimension = "version"
            applicationIdSuffix = ".dev"
            manifestPlaceholders["app_label"] = "PIECE(dev)"
        }
        create("stage") {
            dimension = "version"
            applicationIdSuffix = ".stage"
            manifestPlaceholders["app_label"] = "PIECE\n(stage)"
        }
        create("real") {
            dimension = "version"
            manifestPlaceholders["app_label"] = "PIECE"
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86","x86_64","armeabi-v7a","arm64-v8a")
            isUniversalApk = false
        }
    }

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/DEPENDENCIES.txt",
                "META-INF/dependencies.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/LGPL2.1",
                "META-INF/ASL2.0",
                "build.xml",
                "META-INF/rxjava.properties",
                "META-INF/proguard/androidx-annotations.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    externalNativeBuild {
        cmake {
            path ("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(SDKS.KAKAO_COMMON)
    implementation(SDKS.KAKAO_TALK)
    implementation(Google.MATERIAL)
    implementation(AndroidX.CONSTRAINT_LAYOUT)
    implementation(AndroidX.APPCOMPAT)

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

    androidTestImplementation(Test.ESPRESSO_CORE)

    implementation(Kotlin.COROUTINES_CORE)
    implementation(Kotlin.COROUTINES)

    implementation(AndroidX.WINDOW)
    implementation(AndroidX.CORE)
    implementation(AndroidX.WORK)
    implementation(AndroidX.ACTIVITY)
    implementation(AndroidX.FRAGMENT)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL)
    implementation(AndroidX.LIFECYCLE_LIVEDATA)
    implementation(AndroidX.APPCOMPAT)
    implementation(AndroidX.MULTIDEX)
    implementation(AndroidX.BROWSER)
    implementation(AndroidX.CONSTRAINT_LAYOUT)
    implementation(AndroidX.VIEWPAGER)
    implementation(AndroidX.VIEWPAGER2)
    implementation(AndroidX.LIFECYCLE_EXTENSIONS)
    implementation(AndroidX.COMMON)
    implementation(AndroidX.DATABINDING)
    implementation(AndroidX.SWIPEREFRESH)
    implementation(AndroidX.SPLASHCREEN)
    implementation(AndroidX.BIOMETRIC)
    implementation(AndroidX.BIOMETRIC_KTX)

    implementation(Navigation.NAVIGATION_FRAGMENT_KTX)
    implementation(Navigation.NAVIGATION_FRAGMENT_UI_KTX)

    implementation(Google.MATERIAL)
    implementation(Google.GSON)
    implementation(Google.PLAY)
    implementation(Google.PLAY_KTX)
//    implementation(Google.APP_UPDATE)
//    implementation(Google.APP_UPDATE_KTX)
    implementation(Google.OSS)

    implementation(RxJava2.ANDROID)
    implementation(RxJava2.BINDING)

    implementation(Room.RUNTIME)
    kapt(Room.COMPILER)

    implementation(platform(Firebase.FIREBASE_BOM))
    implementation(Firebase.MESSAGING)
    implementation(Firebase.DATABASE)
    implementation(Firebase.CORE)
//    implementation(Firebase.FIREBASE_ANALYTICS)
    implementation(Firebase.FIREBASE_ANALYTICS_KTX)

    implementation(Dagger.HILT)
    kapt(Dagger.COMPILER)

    implementation(OkHttp.OKHTTP_3)
    implementation(OkHttp.OKHTTP_3_URLCONNECTION)
    implementation(OkHttp.OKHTTP_3_INTERCEPTOR)

    implementation(Retrofit.RETROFIT_2)
    implementation(Retrofit.RETROFIT_2_GSON)
    implementation(Retrofit.RETROFIT_2_SCALARS)
    implementation(Retrofit.RETROFIT_2_SIMPLEXML)
    implementation(Retrofit.CONVERTER_JAXB)

    implementation(Lottie.LOTTIE)

    implementation(Libs.GLIDE)

    implementation(Libs.MPCHART)

    implementation(Jetpack.PAGING)

    implementation(Libs.CARD_VIEW)

    implementation(Libs.PDF_VIEWER)

    implementation(Libs.NAVER_MAP)

    implementation(Libs.FLEX_BOX)

    implementation(Libs.TOOL_TIP) {
        exclude("androidx.fragment")
    }

    implementation(Libs.INDICATOR)
}
