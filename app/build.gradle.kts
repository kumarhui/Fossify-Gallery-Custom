import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile =
    rootProject.file("keystore.properties")

val keystoreProperties =
    Properties()

if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use {
        keystoreProperties.load(it)
    }
}

fun hasSigningVars(): Boolean {
    return providers.environmentVariable("SIGNING_KEY_ALIAS").isPresent &&
        providers.environmentVariable("SIGNING_KEY_PASSWORD").isPresent &&
        providers.environmentVariable("SIGNING_STORE_FILE").isPresent &&
        providers.environmentVariable("SIGNING_STORE_PASSWORD").isPresent
}

android {

    namespace =
        project.property("APP_ID").toString()

    compileSdk =
        project.libs.versions.app.build.compileSDKVersion
            .get()
            .toInt()

    defaultConfig {

        applicationId =
            project.property("APP_ID").toString()

        minSdk =
            project.libs.versions.app.build.minimumSDK
                .get()
                .toInt()

        targetSdk =
            project.libs.versions.app.build.targetSDK
                .get()
                .toInt()

        versionName =
            project.property("VERSION_NAME").toString()

        versionCode =
            project.property("VERSION_CODE")
                .toString()
                .toInt()
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    signingConfigs {

        if (keystorePropertiesFile.exists()) {

            register("release") {

                keyAlias =
                    keystoreProperties
                        .getProperty("keyAlias")

                keyPassword =
                    keystoreProperties
                        .getProperty("keyPassword")

                storeFile =
                    file(
                        keystoreProperties
                            .getProperty("storeFile")
                    )

                storePassword =
                    keystoreProperties
                        .getProperty("storePassword")
            }

        } else if (hasSigningVars()) {

            register("release") {

                keyAlias =
                    providers
                        .environmentVariable(
                            "SIGNING_KEY_ALIAS"
                        )
                        .get()

                keyPassword =
                    providers
                        .environmentVariable(
                            "SIGNING_KEY_PASSWORD"
                        )
                        .get()

                storeFile =
                    file(
                        providers
                            .environmentVariable(
                                "SIGNING_STORE_FILE"
                            )
                            .get()
                    )

                storePassword =
                    providers
                        .environmentVariable(
                            "SIGNING_STORE_PASSWORD"
                        )
                        .get()
            }

        } else {

            logger.warn(
                "Warning: No signing config found. Build will be unsigned."
            )
        }
    }

    buildTypes {

        debug {
            applicationIdSuffix =
                ".debug"
        }

        release {

            isMinifyEnabled =
                true

            isShrinkResources =
                true

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

            if (
                keystorePropertiesFile.exists() ||
                hasSigningVars()
            ) {

                signingConfig =
                    signingConfigs.getByName(
                        "release"
                    )
            }
        }
    }

    flavorDimensions.add(
        "licensing"
    )

    productFlavors {

        register("foss")

        register("gplay")
    }

    sourceSets {

        getByName("main")
            .java
            .directories
            .add(
                "src/main/kotlin"
            )
    }

    compileOptions {

        val currentJavaVersionFromLibs =
            JavaVersion.valueOf(
                libs.versions.app.build.javaVersion.get()
            )

        sourceCompatibility =
            currentJavaVersionFromLibs

        targetCompatibility =
            currentJavaVersionFromLibs
    }

    dependenciesInfo {
        includeInApk = false
    }

    androidResources {

        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    lint {

        checkReleaseBuilds = false

        abortOnError = true

        warningsAsErrors = false

        baseline =
            file("lint-baseline.xml")

        lintConfig =
            rootProject.file("lint.xml")
    }

    packaging {

        resources {

            excludes +=
                "META-INF/library_release.kotlin_module"
        }
    }

    bundle {

        language {

            enableSplit = false
        }
    }
}

/*
 * IMPORTANT:
 * This must stay OUTSIDE android { }.
 *
 * Keeping it here avoids the
 * "Suspicious receiver type" warning.
 */
tasks.withType<KotlinCompile> {

    compilerOptions.jvmTarget.set(
        JvmTarget.fromTarget(
            project.libs.versions.app.build.kotlinJVMTarget.get()
        )
    )
}

dependencies {

    // Fossify
    implementation(
        libs.fossify.commons
    )

    // AndroidX
    implementation(
        libs.androidx.print
    )

    implementation(
        libs.android.image.cropper
    )

    implementation(
        libs.android.gif.drawable
    )

    implementation(
        libs.androidx.lifecycle.runtime
    )

    implementation(
        libs.androidx.constraintlayout
    )

    implementation(
        libs.androidx.documentfile
    )

    implementation(
        libs.androidx.media3.exoplayer
    )

    implementation(
        libs.androidx.swiperefreshlayout
    )

    // EXIF
    implementation(
        libs.exif
    )

    // Image processing
    implementation(
        libs.androidphotofilters
    )

    implementation(
        libs.androidsvg.aar
    )

    implementation(
        libs.gestureviews
    )

    implementation(
        libs.subsamplingscaleimageview
    )

    implementation(
        libs.sanselan
    )

    implementation(
        libs.awebp
    )

    implementation(
        libs.apng
    )

    implementation(
        libs.avif
    )

    implementation(
        libs.avif.integration
    )

    implementation(
        libs.jxl.integration
    )

    implementation(
        libs.okio
    )

    implementation(
        libs.picasso
    ) {
        exclude(
            group = "com.squareup.okhttp3",
            module = "okhttp"
        )
    }

    compileOnly(
        libs.okhttp
    )

    // WebP
    implementation(
        libs.zjupure.webpdecoder
    )

    // Room
    implementation(
        libs.bundles.room
    )

    // Room compiler
    ksp(
        libs.androidx.room.compiler
    )

    // Glide compiler
    ksp(
        libs.glide.compiler
    )

    // Detekt
    detektPlugins(
        libs.compose.detekt
    )

    // ---------------------------------------------------------
    // Jetpack Compose
    // ---------------------------------------------------------

    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        libs.androidx.compose.material.icons.extended
    )

    implementation(
        libs.androidx.activity.compose
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    // ---------------------------------------------------------
    // Google ML Kit - Subject Segmentation
    // Used by Passport Photo background removal.
    // ---------------------------------------------------------

    implementation(
        libs.mlkit.subject.segmentation
    )
}
