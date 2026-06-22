import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.gradle.shadow)
}

kotlin {
    jvmToolchain(24)

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.frontendCompose.shared)

            implementation(libs.compose.ui)
        }

        jvmMain.dependencies {
            implementation(projects.frontendServer)
            implementation(libs.cli)
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn("wasmJsBrowserDistribution")
    from(layout.buildDirectory.dir("dist/wasmJs/productionExecutable")) {
        into("resolver-control")
    }
    archiveFileName.set("resolver-control-wasm.jar")
    manifest {
        attributes["Main-Class"] = "com.resolver.server.ServerKt"
    }
}