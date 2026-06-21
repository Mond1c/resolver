import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.gradle.shadow)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.resolver"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.resolver.MainKt")
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
}

tasks.runShadow {
    standardInput = System.`in`
}

dependencies {
    implementation(libs.logger)

    implementation(projects.server.scoreboardManagement.di)
    implementation(project(":server:resolver-server:di"))
    implementation(project(":server:resolution-logic:di"))
    implementation(project(":server:util:di"))
    implementation(project(":server:scoreboard-management:api"))
    implementation(project(":server:resolver-server:api"))
    implementation(project(":server:resolution-logic:api"))
    implementation(project(":server:util:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}