plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resolver_util"
version = "1.0-util-SNAPSHOT"

dependencies {
    implementation(project(":server:util:api"))
    implementation(project(":server:util:impl"))
    implementation(project(":server:scoreboard-management:api"))
    implementation(project(":server:resolver-server:api"))
    implementation(project(":server:resolution-logic:api"))

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}