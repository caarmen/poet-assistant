/*
 * Copyright (c) 2016-present Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.benmanes)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("jacoco")
    id("kotlin-kapt")
    id("kotlin-android")
}
android {
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        dataBinding = true
        buildConfig = true
    }

    testCoverage {
        jacocoVersion = "0.8.12"
    }
    lint {
        abortOnError = true
        textReport = true
        ignoreWarnings = true
        disable.add("RestrictedApi")  // https://stackoverflow.com/questions/45648530/restricted-api-lint-error-when-deleting-table-room-persistence
        checkReleaseBuilds = false
    }

    defaultConfig {
        applicationId = "ca.rmen.android.poetassistant"
        namespace = "ca.rmen.android.poetassistant"
        minSdk = 21
        targetSdk = 35
        versionCode = 113101
        versionName = "1.31.1"
        // setting vectorDrawables.useSupportLibrary = true means pngs won"t be generated at
        // build time: http://android-developers.blogspot.fr/2016/02/android-support-library-232.html
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app"s state is completely cleared between tests.
        // https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner#use-android
        // testInstrumentationRunnerArguments clearPackageData: "true", coverage: "true", coverageFilePath: "/data/data/ca.rmen.android.poetassistant.test/"


        // used by Room, to test migrations
        sourceSets {
            getByName("main") {
                java.srcDirs(listOf("$projectDir/src/main/kotlin"))
                assets.srcDirs(project.layout.buildDirectory.dir("generated/license_assets"))
            }
            getByName("androidTest") {
                assets.srcDirs(files("$projectDir/src/androidTest/schemas"))
                java.srcDirs(
                    "$projectDir/src/androidTest/kotlin",
                    "$projectDir/src/sharedTest/java",
                    "$projectDir/src/sharedTest/kotlin",
                )
            }
            getByName("test") {
                manifest.srcFile("src/test/AndroidManifest.xml")
                java.srcDirs(
                    listOf(
                        "$projectDir/src/test/kotlin",
                        "$projectDir/src/sharedTest/java",
                        "$projectDir/src/sharedTest/kotlin",
                    )
                )
            }
        }

        // used by Room, to test migrations
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = projectDir.resolve("schemas").toString()
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = project.gradle.startParameter.taskNames.any {
                it.contains("jacocoTestReport")
            }
            enableAndroidTestCoverage = project.gradle.startParameter.taskNames.any {
                it.contains("jacocoTestReport")
            }
            applicationIdSuffix = ".test"
            resValue(
                "string",
                "search_provider_authority",
                "${android.defaultConfig.applicationId}${applicationIdSuffix}.SuggestionsProvider"
            )
        }
        release {
            if (rootProject.hasProperty("AndroidSigningKeyAlias")
                && rootProject.hasProperty("AndroidSigningKeyPassword")
                && rootProject.hasProperty("AndroidSigningStoreFile")
                && rootProject.hasProperty("AndroidSigningStorePassword")
            ) {
                println("Using signing properties from gradle properties")
                signingConfigs {
                    create("release") {
                        keyAlias = rootProject.extra["AndroidSigningKeyAlias"] as String
                        keyPassword = rootProject.extra["AndroidSigningKeyPassword"] as String
                        storeFile = file(rootProject.extra["AndroidSigningStoreFile"] as String)
                        storePassword = rootProject.extra["AndroidSigningStorePassword"] as String
                    }
                }
            } else if (System.getenv("AndroidSigningKeyPassword") != null
                && System.getenv("AndroidSigningKeyPassword") != null
                && System.getenv("AndroidSigningStoreFile") != null
                && System.getenv("AndroidSigningStorePassword") != null
            ) {
                println("Using signing properties from environment variables")
                signingConfigs {
                    create("release") {
                        keyAlias = System.getenv("AndroidSigningKeyAlias")
                        keyPassword = System.getenv("AndroidSigningKeyPassword")
                        storeFile = file(System.getenv("AndroidSigningStoreFile"))
                        storePassword = System.getenv("AndroidSigningStorePassword")
                    }
                }
            } else {
                println("No signing properties found")
            }
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue(
                "string",
                "search_provider_authority",
                android.defaultConfig.applicationId + ".SuggestionsProvider"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    testOptions {
        // Uncomment below to use orchestrator for tests
        // execution "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests {
            all {
                it.jvmArgs("-noverify", "-ea")
            }
            isIncludeAndroidResources = true
        }
    }


}

jacoco {
    toolVersion = "0.8.12"
}
android.applicationVariants.configureEach {
    val copyLicenseFilesTask = tasks.register<Copy>("copyLicenseFilesFor${name.replaceFirstChar{it.uppercase()}}") {
        from(project.rootDir)
        into(project.layout.buildDirectory.dir("generated/license_assets/"))
        include("LICENSE.txt")
        include("LICENSE-rhyming-dictionary.txt")
        include("LICENSE-thesaurus-wordnet.txt")
        include("LICENSE-dictionary-wordnet.txt")
        include("LICENSE-google-ngram-dataset.txt")
    }
    mergeAssetsProvider.configure {
        dependsOn(copyLicenseFilesTask)
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.room.runtime)
    implementation(libs.dagger)
    implementation(libs.google.material)
    implementation(libs.kotlin)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.porter.stemmer)
    implementation(libs.rhymer)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // We need to explicitly add a couple of api dependencies here, otherwise alpha versions
    // of these libs will be pulled in transitively (by a non-alpha databinding dependency...)
    api(libs.androidx.collection)
    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.lifecycle.viewmodel.ktx)

    ksp(libs.androidx.room.compiler)
    ksp(libs.dagger.compiler)

    kspTest(libs.dagger.compiler)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.test.espresso.contrib)
    testImplementation(libs.androidx.test.espresso.core)
    testImplementation(libs.androidx.test.espresso.intents)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.ui.test.junit4.android)
    testImplementation(libs.fest.reflect)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.fest.reflect)
    androidTestImplementation(libs.google.test.parameter.injector)
    androidTestImplementation(libs.robolectric.annotations)

    androidTestUtil(libs.androidx.test.orchesetrator)
}

// Only show real releases with the ben-manes plugin.
tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    resolutionStrategy {
        componentSelection {
            all { selection: ComponentSelection ->
                var rejected = listOf("alpha", "alpha-preview", "beta", "rc", "cr", "m", "eap", "dev").any { qualifier ->
                    selection.candidate.version.matches(Regex("(?i).*[.-]${qualifier}[.\\d-]*"))
                }
                if ("com.android.databinding" == selection.candidate.group) {
                    rejected = true
                }
                if (rejected) {
                    selection.reject("Release candidate")
                }
            }
        }
    }
}
tasks.withType<Test>().configureEach {
    // Copied from the now in android app config:
    // https://github.com/android/nowinandroid/blob/main/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/Jacoco.kt
    configure<JacocoTaskExtension> {
        // Required for JaCoCo + Robolectric
        // https://github.com/robolectric/robolectric/issues/2230
        isIncludeNoLocationClasses = true

        // Required for JDK 11 with the above
        // https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
        excludes = listOf("jdk.internal.*")
    }
}
tasks.register<JacocoReport>("jacocoTestReport") {
    mustRunAfter("testDebugUnitTest")
    mustRunAfter("connectedDebugAndroidTest")
    mustRunAfter("createDebugCoverageReport")
    classDirectories.setFrom(
        fileTree(mapOf(
            "dir" to layout.buildDirectory,
            "includes" to listOf("tmp/kotlin-classes/debug/ca/rmen/android/poetassistant/**/*.class",
                "intermediates/javac/debug/compileDebugWithJavac/classes/ca/rmen/android/poetassistant/**/*.class"),
            "excludes" to listOf("**/R.class", "**/R*.class", "**/Manifest.class", "**/Manifest*.class", "**/BuildConfig.class",
                // ignore databinding generated code:
                "**/ca/rmen/android/poetassistant/databinding/*.class",
                "**/ca/rmen/android/poetassistant/BR.class",
                "**/ca/rmen/android/poetassistant/DataBinderMapperImpl.class",
                "**/ca/rmen/android/poetassistant/DataBinderMapperImpl\$*.class",
                "**/*_Impl*.class",
                // ignore dagger generated code:
                "**/ca/rmen/android/poetassistant/**/DaggerAppComponent*.class",
                "**/ca/rmen/android/poetassistant/**/*_Factory.class",
                "**/ca/rmen/android/poetassistant/**/*_Provides*.class",
                "**/ca/rmen/android/poetassistant/**/*Injector.class",
                // ignore generated code not in our package
                "**/android/databinding/*.class",
                "**/android/databinding/**/*.class",
                "**/com/android/**/*.class")
        ))
    )
    sourceDirectories.setFrom(
        files(
            "${project.projectDir}/src/main/java",
            "${project.projectDir}/src/main/kotlin"
        )
    )
    executionData.setFrom(
        fileTree(mapOf(
            "dir" to layout.buildDirectory,
            "includes" to listOf(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/**/*.ec"
            )
        ))
    )
    reports {
        xml.required = true
    }
}
