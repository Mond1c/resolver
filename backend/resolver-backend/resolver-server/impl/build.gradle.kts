plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_server"
version = "1.0-resolver-SNAPSHOT"

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)

    implementation(project(":resolver-server:api"))
    implementation(project(":scoreboard-management:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}