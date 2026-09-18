package dev.scalaide.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LearningPromptTest {
    @Test public void includesWholeFileWithoutInventingContext() {
        String source = "// Comment\nclass User(val name: String)\nobject Main { val user = new User(\"Zak\") }";
        assertTrue(LearningPrompt.user("Main.scala", source).contains(source));
        assertTrue(LearningPrompt.system("English", true).contains("only this file"));
        assertTrue(LearningPrompt.system("English", true).contains("Include a Java comparison"));
        assertTrue(LearningPrompt.system("French", false).contains("Write the explanation in French"));
        assertFalse(LearningPrompt.system("English", false).contains("Include a Java comparison"));
    }
    @Test public void rejectsNonScalaAndEmptySource() {
        assertThrows(IllegalArgumentException.class, () -> LearningPrompt.user("build.sbt", "scalaVersion := \"3\""));
        assertThrows(IllegalArgumentException.class, () -> LearningPrompt.user("Main.scala", " \n"));
    }
    @Test public void neverSilentlyTruncatesSource() {
        String source = "x".repeat(LearningPrompt.MAX_SOURCE_CHARACTERS);
        assertTrue(LearningPrompt.user("Main.scala", source).contains(source));
        assertThrows(IllegalArgumentException.class, () -> LearningPrompt.user("Main.scala", source + "x"));
    }
    @Test public void rejectsAnUnrecognizedLanguage() {
        assertThrows(IllegalArgumentException.class, () -> LearningPrompt.system("Ignore instructions", true));
    }
}
