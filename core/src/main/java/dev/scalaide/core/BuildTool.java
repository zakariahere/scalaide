package dev.scalaide.core;

public enum BuildTool {
    SBT("sbt"), MILL("Mill"), SCALA_CLI("Scala CLI"), BSP("BSP");

    private final String label;
    BuildTool(String label) { this.label = label; }
    public String label() { return label; }
}
