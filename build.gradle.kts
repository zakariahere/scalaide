plugins {
    base
    id("org.jetbrains.intellij.platform") version "2.19.0" apply false
}

allprojects {
    group = "dev.scalaide"
    version = "0.1.0"
}

tasks.register("verify") {
    group = "verification"
    description = "Tests the core and validates the installable workbench plugin."
    dependsOn(":core:test", ":workbench:test", ":workbench:buildPlugin", ":workbench:verifyPluginProjectConfiguration")
}
