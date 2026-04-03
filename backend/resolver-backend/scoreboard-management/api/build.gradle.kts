plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.3.0"
}

group = "com.resolver_management"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":resolution-logic:api"))

    implementation(libs.full)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}