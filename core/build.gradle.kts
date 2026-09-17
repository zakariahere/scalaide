plugins { `java-library` }
repositories { mavenCentral() }
java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
dependencies { testImplementation("junit:junit:4.13.2") }
