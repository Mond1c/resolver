plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":resolution-logic:api"))
    implementation(project(":resolution-logic:impl"))

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