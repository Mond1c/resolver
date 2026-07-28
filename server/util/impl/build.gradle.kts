plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":server:util:api"))
    implementation(project(":server:resolver-server:api"))
    implementation(project(":server:resolution-logic:api"))
    implementation(project(":server:scoreboard-management:api"))

    implementation(libs.serialization)
    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}