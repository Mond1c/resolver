plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_management"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":scoreboard-management:api"))
    implementation(project(":scoreboard-management:impl"))

    implementation(libs.clics.api)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}