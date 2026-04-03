plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_util"
version = "1.0-util-SNAPSHOT"

dependencies {
    implementation(libs.full)

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}