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

    implementation(project(":resolver-server:api"))
    implementation(project(":resolver-server:di"))
    implementation(project(":resolution-logic:api"))
    implementation(project(":resolution-logic:di"))
    implementation(project(":scoreboard-management:api"))
    implementation(project(":scoreboard-management:di"))
    implementation(project(":util:api"))
    implementation(project(":util:di"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}