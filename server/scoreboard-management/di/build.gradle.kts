plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":server:resolution-logic:api"))
    implementation(project(":server:scoreboard-management:api"))
    implementation(project(":server:scoreboard-management:impl"))
    implementation(project(":server:util:api"))
    implementation(project(":server:util:di"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}