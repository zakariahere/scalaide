package dev.scalaide.workbench;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

/** Local, owner-provided brand assets. No network requests or font installation. */
final class BrandIdentity {
    private static final Logger LOG = Logger.getInstance(BrandIdentity.class);
    static final Color TEXT = color("text", 0x101827, 0xF5F7FB);
    static final Color SECONDARY = color("secondary", 0x46546C, 0xB3C0D4);
    static final Color MUTED = color("muted", 0x5D6B82, 0x93A4BF);
    static final Color ACCENT = color("accent", 0x315CFD, 0xA8C7FF);
    static final Color BORDER = color("border", 0xD5DEEC, 0x28354B);
    static final Color RAISED = color("raised", 0xFFFFFF, 0x121B29);

    private static final Font DISPLAY = loadFont("SpaceGrotesk.ttf");
    private static final Font BODY = loadFont("HankenGrotesk.ttf");
    private static final BufferedImage MASCOT = loadMascot();

    private BrandIdentity() {}

    static Font display(float size) { return DISPLAY.deriveFont(Font.BOLD, (float) JBUI.scale((int) size)); }
    static Font body(float size) { return BODY.deriveFont(Font.PLAIN, (float) JBUI.scale((int) size)); }

    private static Color color(String key, int light, int dark) {
        return JBColor.namedColor("ScalaWorkbench." + key, new JBColor(new Color(light), new Color(dark)));
    }
    private static Font loadFont(String name) {
        try (var stream = BrandIdentity.class.getResourceAsStream("/branding/fonts/" + name)) {
            return Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(stream, name));
        } catch (Exception failure) {
            LOG.warn("Cannot load bundled brand font " + name, failure);
            return new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
    }
    private static BufferedImage loadMascot() {
        try (var stream = BrandIdentity.class.getResourceAsStream("/branding/zak-hoodie.png")) {
            var image = ImageIO.read(Objects.requireNonNull(stream, "Mascot resource"));
            if (image == null) throw new IOException("Cannot decode mascot PNG");
            return image;
        } catch (IOException failure) { throw new IllegalStateException("Cannot load the bundled mascot", failure); }
    }

    static Icon mascot() {
        // Scale only when painting, preserving the original pixel data and HiDPI rendering.
        return new Icon() {
            @Override public int getIconWidth() { return JBUI.scale(78); }
            @Override public int getIconHeight() { return JBUI.scale(119); }
            @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
                var g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.drawImage(MASCOT, x, y, getIconWidth(), getIconHeight(), null);
                } finally { g.dispose(); }
            }
        };
    }
}
