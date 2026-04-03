plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_management"
version = "1.0-management-SNAPSHOT"

dependencies {
    implementation(project(":resolution-logic:api"))
    implementation(project(":scoreboard-management:api"))
    implementation(project(":util:api"))

    implementation(libs.full)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}