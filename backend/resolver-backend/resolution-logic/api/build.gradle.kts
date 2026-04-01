plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}