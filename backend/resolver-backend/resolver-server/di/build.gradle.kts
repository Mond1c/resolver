plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_server"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":resolver-server:api"))
    implementation(project(":resolver-server:impl"))
    implementation(project(":scoreboard-management:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}