plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core)
    implementation(project(":server:resolution-logic:api"))
    implementation(project(":server:scoreboard-management:api"))
    implementation(project(":server:util:api"))

    implementation(libs.full)

    implementation(libs.coroutines)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":server:util:di"))
    testImplementation(project(":server:scoreboard-management:di"))
    testImplementation(project(":server:resolution-logic:di"))
    testImplementation(libs.logger)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}