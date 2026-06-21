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

    implementation(projects.core)
    implementation(project(":server:util:api"))
    implementation(project(":server:resolver-server:api"))
    implementation(project(":server:scoreboard-management:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":server:util:di"))
    testImplementation(project(":server:scoreboard-management:di"))
    testImplementation(project(":server:resolution-logic:di"))
    testImplementation(project(":server:resolution-logic:api"))
    testImplementation(project(":server:resolver-server:di"))
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