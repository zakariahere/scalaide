package dev.scalaide.workbench;

/** A deliberately small Markdown view. Escaping happens before formatting; HTML/URLs never execute. */
final class LearningMarkdown {
    private LearningMarkdown() {}
    static String body(String markdown) {
        var html = new StringBuilder(); boolean code = false; boolean list = false;
        for (String line : markdown.split("\\R", -1)) {
            if (line.stripLeading().startsWith("```")) {
                if (list) { html.append("</ul>"); list = false; }
                html.append(code ? "</pre>" : "<pre>"); code = !code; continue;
            }
            if (code) { html.append(escape(line)).append('\n'); continue; }
            boolean item = line.matches("^\\s*([-*]|\\d+[.)])\\s+.*");
            if (list && !item) { html.append("</ul>"); list = false; }
            if (line.isBlank()) continue;
            if (item) {
                if (!list) { html.append("<ul>"); list = true; }
                html.append("<li>").append(inline(line.replaceFirst("^\\s*([-*]|\\d+[.)])\\s+", ""))).append("</li>");
            } else if (line.matches("^#{1,6} .*")) {
                html.append("<h2>").append(inline(line.replaceFirst("^#+\\s+", ""))).append("</h2>");
            } else html.append("<p>").append(inline(line)).append("</p>");
        }
        if (list) html.append("</ul>"); if (code) html.append("</pre>");
        return html.toString();
    }
    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    private static String inline(String text) {
        return escape(text).replaceAll("`([^`]+)`", "<code>$1</code>").replaceAll("\\*\\*([^*]+)\\*\\*", "<b>$1</b>");
    }
}
