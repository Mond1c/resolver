rootProject.name = "Resolver"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app:desktopApp")
include(":app:shared")
include(":app:resolverControlWasm")
include(":app:resolverWasm")

include(":core")

include(":server")
include(":server:resolution-logic:api")
include(":server:resolution-logic:impl")
include(":server:resolution-logic:di")
include(":server:scoreboard-management:api")
include(":server:scoreboard-management:impl")
include(":server:scoreboard-management:di")
include(":server:util:api")
include(":server:util:di")
include(":server:util:impl")
include(":server:resolver-server:api")
include(":server:resolver-server:di")
include(":server:resolver-server:impl")

include(":frontend-react")
include(":frontend-server")
