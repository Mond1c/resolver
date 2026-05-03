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

    implementation(project(":util:api"))
    implementation(project(":resolver-server:api"))
    implementation(project(":scoreboard-management:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":util:di"))
    testImplementation(project(":scoreboard-management:di"))
    testImplementation(project(":resolution-logic:di"))
    testImplementation(project(":resolution-logic:api"))
    testImplementation(project(":resolver-server:di"))
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.cio)
    testImplementation(libs.logger)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}