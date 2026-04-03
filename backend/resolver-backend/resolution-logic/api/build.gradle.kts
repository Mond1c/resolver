plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "com.resolver"
version = "1.0-logic-SNAPSHOT"

application {
    mainClass.set("com.resolver.MainKt")
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    implementation(libs.full)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}