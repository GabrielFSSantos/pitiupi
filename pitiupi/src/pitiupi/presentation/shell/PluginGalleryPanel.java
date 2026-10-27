package pitiupi.presentation.shell;

import pitiupi.models.Plugin;
import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Aba Plugins: tiles compactos para abrir/focar aba do plugin.
 */
public final class PluginGalleryPanel extends JPanel {

    private BiConsumer<String, String> onPluginSelected;

    public PluginGalleryPanel(BiConsumer<String, String> onPluginSelected) {
        this.onPluginSelected = onPluginSelected;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(6, 6, 6, 6));
    }

    public void setOpenHandler(BiConsumer<String, String> onPluginSelected) {
        this.onPluginSelected = onPluginSelected;
    }

    public void setPlugins(List<Plugin> plugins) {
        removeAll();
        if (plugins.isEmpty()) {
            add(new JLabel(
                    "<html><center>Escolha um plugin para abrir aqui.<br/>"
                            + "Tudo acontece nesta área — sem janelas extras.</center></html>",
                    SwingConstants.CENTER
            ), BorderLayout.CENTER);
            return;
        }
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        List<Plugin> ordered = plugins.stream()
                .sorted(Comparator.comparingInt(PluginGalleryPanel::galleryOrder))
                .toList();
        for (Plugin plugin : ordered) {
            String display = plugin.getDisplayName();
            JButton btn = new JButton(display);
            ShellUi.applyPluginGalleryTile(btn);
            btn.setToolTipText(buildTooltip(plugin));
            btn.addActionListener(e -> onPluginSelected.accept(plugin.getIdentifier(), display));
            grid.add(btn);
        }
        JScrollPane scroll = new JScrollPane(grid);
        ShellUi.borderlessScroll(scroll);
        add(scroll, BorderLayout.CENTER);
    }

    private static String buildTooltip(Plugin plugin) {
        return "<html>"
                + plugin.getDisplayName()
                + "<br/><i>" + plugin.getName() + "</i>"
                + "<br/>ID: " + plugin.getIdentifier()
                + "<br/>v" + plugin.getVersion() + " · " + plugin.getAuthor()
                + "</html>";
    }

    private static int galleryOrder(Plugin plugin) {
        if (plugin.getIdentifier() != null && plugin.getIdentifier().toLowerCase().contains("chat")) {
            return 0;
        }
        if (plugin.getIdentifier() != null && plugin.getIdentifier().toLowerCase().contains("screencast")) {
            return 1;
        }
        return 2;
    }
}
