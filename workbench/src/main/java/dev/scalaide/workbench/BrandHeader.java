package dev.scalaide.workbench;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/** A compact native Swing brand lockup; source editing remains visually quiet. */
final class BrandHeader extends JPanel {
    BrandHeader() {
        super(new BorderLayout(0, JBUI.scale(10)));
        setBackground(BrandIdentity.RAISED);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrandIdentity.BORDER), JBUI.Borders.empty(14)));

        var identity = new JPanel(new BorderLayout(JBUI.scale(12), 0));
        identity.setOpaque(false);
        identity.add(new JBLabel(IconLoader.getIcon("/branding/zb.svg", BrandHeader.class)), BorderLayout.WEST);
        var owner = new JBLabel("ZAKARIA / SCALA WORKBENCH");
        owner.setFont(BrandIdentity.body(11));
        owner.setForeground(BrandIdentity.ACCENT);
        identity.add(owner, BorderLayout.CENTER);
        add(identity, BorderLayout.NORTH);

        var center = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        center.setOpaque(false);
        var words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new BoxLayout(words, BoxLayout.Y_AXIS));
        words.add(Box.createVerticalGlue());
        for (String line : new String[]{"Stay curious.", "Keep building."}) {
            var label = new JBLabel(line);
            label.setFont(BrandIdentity.display(23));
            label.setForeground(line.startsWith("Keep") ? BrandIdentity.ACCENT : BrandIdentity.TEXT);
            words.add(label);
        }
        words.add(Box.createVerticalStrut(JBUI.scale(8)));
        var site = new JBLabel("zakaria.lu");
        site.setFont(BrandIdentity.body(12));
        site.setForeground(BrandIdentity.MUTED);
        words.add(site);
        words.add(Box.createVerticalGlue());
        center.add(words, BorderLayout.CENTER);
        var mascot = new JBLabel(BrandIdentity.mascot());
        mascot.getAccessibleContext().setAccessibleName("Zakaria's navy-hoodie mascot, waving with a laptop");
        center.add(mascot, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
        setPreferredSize(new Dimension(JBUI.scale(320), JBUI.scale(188)));
        setMinimumSize(new Dimension(JBUI.scale(270), JBUI.scale(188)));
        getAccessibleContext().setAccessibleName("Zakaria Scala Workbench. Stay curious. Keep building.");
    }
}
