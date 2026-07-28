plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(24)
}

dependencies {
    implementation(libs.cli)

    implementation(libs.logger)

    implementation(libs.coroutines)

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
}