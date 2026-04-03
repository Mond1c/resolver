plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)

    implementation(project(":resolver-server:api"))
    implementation(project(":resolver-server:di"))
    implementation(project(":resolution-logic:api"))
    implementation(project(":resolution-logic:di"))
    implementation(project(":scoreboard-management:api"))
    implementation(project(":scoreboard-management:di"))
    implementation(project(":util:api"))
    implementation(project(":util:di"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}