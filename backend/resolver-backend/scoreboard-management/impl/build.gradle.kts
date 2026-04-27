plugins {
    alias(libs.plugins.kotlin.jvm)
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
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation(project(":util:di"))
    testImplementation(project(":scoreboard-management:di"))
    testImplementation(project(":resolution-logic:di"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}