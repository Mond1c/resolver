plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "resolver-backend"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

include(":resolution-logic:api")
include(":resolution-logic:impl")
include(":resolution-logic:di")
include(":scoreboard-management:api")
include(":scoreboard-management:impl")
include(":scoreboard-management:di")
include(":util:api")
include(":util:di")
include(":util:impl")
include(":resolver-server:api")
include(":resolver-server:di")
include(":resolver-server:impl")
