import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * Syncs the sibling data/content curriculum repo into an AGP-managed generated assets
 * directory. AGP wires [outputDir] and the task dependency via addGeneratedSourceDirectory,
 * so this runs before asset merging without a manual dependsOn. Files land under
 * droidquest/content so content-index.json "content/…" paths resolve against droidquest/.
 */
abstract class SyncDroidQuestContentTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val fs: FileSystemOperations

    @TaskAction
    fun run() {
        fs.sync {
            from(sourceDir) { include("**/*.json") }
            into(outputDir.dir("droidquest/content"))
        }
    }
}

android {
    namespace = "dev.novanest.droidquest"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.novanest.droidquest"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// ── Build-time content sync ──────────────────────────────────────────────
// The data repository stays the single source of truth; nothing is duplicated into
// source control. AGP's Variant API wires the generated assets directory and the task
// dependency (runs before asset merge).
val droidQuestContentDir =
    rootProject.projectDir.parentFile.resolve("data/content")

val syncDroidQuestContent =
    tasks.register<SyncDroidQuestContentTask>("syncDroidQuestContent") {
        description = "Sync DroidQuest curriculum JSON from data/content into generated assets."
        group = "droidquest"
        sourceDir.set(layout.dir(provider { droidQuestContentDir }))
    }

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(
            syncDroidQuestContent,
            SyncDroidQuestContentTask::outputDir,
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
