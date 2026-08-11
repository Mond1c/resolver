plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.resolver_management"
version = "1.0-management-SNAPSHOT"

dependencies {
    implementation(project(":resolution-logic:api"))
    implementation(project(":scoreboard-management:api"))
    implementation(project(":util:api"))

    implementation(libs.full)

    implementation(libs.coroutines)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":util:di"))
    testImplementation(project(":scoreboard-management:di"))
    testImplementation(project(":resolution-logic:di"))
    testImplementation(libs.logger)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}