plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver"
version = "1.0-logic-SNAPSHOT"

dependencies {
    implementation(project(":server:util:api"))
    implementation(project(":server:resolution-logic:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}