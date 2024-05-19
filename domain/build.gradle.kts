plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Retrofit.RETROFIT_2)
    implementation(Jetpack.PAGING_TEST)

    implementation(Room.RUNTIME)
    kapt(Room.COMPILER)

    // (Required) Writing and executing Unit Tests on the JUnit Platform
    testImplementation(Test.JUNIT_5_API)
    testRuntimeOnly(Test.JUNIT_5_ENGINE)

    testImplementation(Test.JUNIT_5_PARAMS)

    // (Optional) If you also have JUnit 4-based tests
    testImplementation(Test.JUNIT_4)
    testRuntimeOnly(Test.JUNIT_5_VINTAGE)

    testImplementation(Test.MOCKK)

    testImplementation(Test.TEST_KOTLIN)
}