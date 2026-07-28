plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
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