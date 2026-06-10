import com.github.gradle.node.pnpm.task.PnpmTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.node)
    alias(libs.plugins.gradle.shadow)
    base
}

repositories {
    mavenCentral()
}

node {
    version = "22.20.0"
    pnpmVersion.set("10.18.3")
    download = true
}

fun PnpmTask.setInputs(directory: Directory, publicUrl: String? = null) {
    environment.set(buildMap {
        publicUrl?.let { put("PUBLIC_URL", it) }
        put("BUILD_PATH", "dist")
    })
    with(layout.projectDirectory) {
        inputs.dir(dir("common"))
        inputs.dir(dir("generated"))
        inputs.file(file("package.json"))
        inputs.file(file("pnpm-lock.yaml"))
    }
    inputs.dir(directory.dir("src"))
    inputs.file(directory.file("package.json"))
}

fun TaskContainerScope.pnpmBuild(
    name: String,
    directory: Directory,
    publicUrl: String,
    configure: PnpmTask.(Directory) -> Unit = {}
) = register<PnpmTask>(name) {
    dependsOn("pnpmInstall")
    outputs.cacheIf { true }
    setInputs(directory, publicUrl)
    workingDir.set(directory.asFile)
    args = listOf("run", "build")
    outputs.dir(directory.dir("dist"))
    configure(directory)
}

kotlin {
    jvmToolchain(24)
}

val cleanResolverDist by tasks.registering(Delete::class) {
    delete(layout.projectDirectory.dir("resolver/dist"))
}

val cleanResolverControlDist by tasks.registering(Delete::class) {
    delete(layout.projectDirectory.dir("resolver-control/dist"))
}

tasks {
    pnpmInstall {
        inputs.file("package.json")
        inputs.file("resolver/package.json")
        inputs.file("resolver-control/package.json")
        nodeModulesOutputFilter {
            exclude("**")
        }
    }

    val buildResolver = pnpmBuild("pnpm_run_buildResolver", layout.projectDirectory.dir("resolver"), "/resolver") {
        dependsOn(cleanResolverDist)
    }

    val buildResolverControl = pnpmBuild(
        "pnpm_run_buildResolverControl",
        layout.projectDirectory.dir("resolver-control"),
        "/resolver-control"
    ) {
        dependsOn(buildResolver, cleanResolverControlDist)
    }

    val resolverJar = register<ShadowJar>("resolverJar") {
        dependsOn(buildResolver, classes)
        archiveBaseName.set("resolver-frontend")
        destinationDirectory.set(layout.buildDirectory.dir("jars"))
        manifest {
            attributes["Main-Class"] = "com.resolver.main.resolver.ResolverFrontendServerKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(project.configurations.runtimeClasspath.get())
        from(sourceSets.main.get().output)
        from(layout.projectDirectory.dir("resolver/dist")) {
            into("resolver")
        }
    }

    val resolverControlJar = register<ShadowJar>("resolverControlJar") {
        dependsOn(buildResolverControl, classes)
        archiveBaseName.set("resolver-control-frontend")
        destinationDirectory.set(layout.buildDirectory.dir("jars"))
        manifest {
            attributes["Main-Class"] = "com.resolver.main.control.ResolverControlFrontendServerKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(project.configurations.runtimeClasspath.get())
        from(sourceSets.main.get().output)
        from(layout.projectDirectory.dir("resolver-control/dist")) {
            into("resolver-control")
        }
    }

    assemble {
        dependsOn(
            buildResolver,
            buildResolverControl,
            resolverJar,
            resolverControlJar
        )
    }
}

dependencies {
    implementation(libs.cli)

    implementation(libs.logger)

    implementation(libs.coroutines)

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
}