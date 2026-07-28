plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":server:util:api"))
    implementation(project(":server:util:di"))
    implementation(project(":server:resolution-logic:api"))
    implementation(project(":server:resolution-logic:impl"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}