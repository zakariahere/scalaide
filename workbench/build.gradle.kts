import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    java
    id("org.jetbrains.intellij.platform")
}

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }

dependencies {
    implementation(project(":core"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    intellijPlatform {
        val localIde = providers.gradleProperty("localIdePath").orElse(providers.environmentVariable("SCALAIDE_IDEA_HOME"))
        if (localIde.isPresent) local(localIde.get())
        else intellijIdea(providers.gradleProperty("platformVersion"))
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.plugins.terminal")
        plugin("org.intellij.scala", providers.gradleProperty("scalaPluginVersion").get())
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "dev.scalaide.workbench"
        name = "Scala Workbench"
        version = project.version.toString()
        ideaVersion { sinceBuild = "262.9437"; untilBuild = "262.*" }
    }
    autoReload = false
}

tasks.runIde {
    maxHeapSize = "3g"
    providers.gradleProperty("scalaProject").orNull?.let { args(it) }
}

tasks.withType<JavaCompile>().configureEach { options.compilerArgs.add("-Xlint:deprecation") }
