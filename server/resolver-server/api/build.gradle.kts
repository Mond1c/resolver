plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_server"
version = "1.0-resolver-SNAPSHOT"

dependencies {
    implementation(project(":server:util:api"))
    implementation(project(":server:scoreboard-management:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}