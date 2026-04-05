plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.resolver_management"
version = "1.0-management-SNAPSHOT"

dependencies {
    implementation(project(":resolution-logic:api"))

    implementation(libs.full)

    api(libs.serialization)

    implementation(libs.coroutines)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}