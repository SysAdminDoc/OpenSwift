import java.util.Locale

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseKeystorePath = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull
val releaseKeystorePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(
    releaseKeystorePath,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

val releaseApkBudgetBytes = 15L * 1024L * 1024L

android {
    namespace = "com.openswift.keyboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.openswift.keyboard"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "0.3.5"
        buildConfigField("boolean", "ENABLE_EXPERIMENTAL_SYNC", "false")
        buildConfigField("boolean", "ENABLE_EXPERIMENTAL_PLUGINS", "false")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

fun releaseApk(): File {
    val expectedSuffix = if (hasReleaseSigning) "-release.apk" else "-release-unsigned.apk"
    val apks = fileTree(layout.buildDirectory.dir("outputs/apk/release")) {
        include("*.apk")
    }.files.filter { it.name.endsWith(expectedSuffix) }
    return apks.singleOrNull()
        ?: throw GradleException(
            "Expected one release APK ending in $expectedSuffix, found ${apks.size}: ${apks.joinToString()}",
        )
}

tasks.register("reportReleaseApkSize") {
    group = "verification"
    description = "Builds the release APK and records its compressed size."
    dependsOn("assembleRelease")

    doLast {
        val apk = releaseApk()
        val sizeBytes = apk.length()
        val sizeMiB = sizeBytes.toDouble() / (1024.0 * 1024.0)
        val sizeMiBFormatted = String.format(Locale.ROOT, "%.2f", sizeMiB)
        val report = layout.buildDirectory.file("reports/apk-size/release.txt").get().asFile
        report.parentFile.mkdirs()
        report.writeText(
            "apk=${apk.name}\nbytes=$sizeBytes\nmib=$sizeMiBFormatted\n" +
                "budget_bytes=$releaseApkBudgetBytes\nbudget_mib=15.00\n",
        )
        logger.lifecycle(
            "Release APK: {} bytes ({} MiB); report: {}",
            sizeBytes,
            sizeMiBFormatted,
            report,
        )
    }
}

tasks.register("checkReleaseApkSize") {
    group = "verification"
    description = "Fails when the release APK exceeds the 15 MiB compressed-size budget."
    dependsOn("reportReleaseApkSize")

    doLast {
        val apk = releaseApk()
        if (apk.length() > releaseApkBudgetBytes) {
            throw GradleException(
                "Release APK is ${apk.length()} bytes; budget is $releaseApkBudgetBytes bytes (15 MiB).",
            )
        }
        logger.lifecycle("Release APK is within the 15 MiB budget.")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.security:security-crypto:1.1.0")
    compileOnly("com.google.errorprone:error_prone_annotations:2.50.0")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
