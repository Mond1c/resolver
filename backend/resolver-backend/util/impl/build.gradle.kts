plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_util"
version = "1.0-util-SNAPSHOT"

dependencies {
    implementation(project(":util:api"))
    implementation(project(":resolver-server:api"))
    implementation(project(":resolution-logic:api"))
    implementation(project(":scoreboard-management:api"))

    implementation(project(":util:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}