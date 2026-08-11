plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_management"
version = "1.0-management-SNAPSHOT"

dependencies {
    implementation(project(":resolution-logic:api"))
    implementation(project(":scoreboard-management:api"))
    implementation(project(":scoreboard-management:impl"))
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