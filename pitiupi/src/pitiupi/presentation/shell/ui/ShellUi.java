package pitiupi.presentation.shell.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Estilo visual compartilhado da shell (Swing nativo / Nimbus).
 */
public final class ShellUi {

    private static final Color CLOSE_HOVER = new Color(180, 40, 40);
    private static final String NOTIFICATION_DOT_COLOR = "#e53935";
    private static final String NOTIFICATION_COUNT_COLOR = "#757575";
    private static final Dimension GALLERY_TILE_SIZE = new Dimension(140, 38);

    private ShellUi() {
    }

    public static Color shellEdgeColor() {
        Color edge = UIManager.getColor("TabbedPane.borderHighlightColor");
        if (edge == null) {
            edge = UIManager.getColor("Component.borderColor");
        }
        if (edge == null) {
            edge = Color.GRAY;
        }
        return edge;
    }

    public static JPanel sectionPanel(String title) {
        JPanel panel = new JPanel();
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(2, 0, 0, 0),
                title,
                TitledBorder.LEADING,
                TitledBorder.TOP
        );
        titled.setTitleFont(panel.getFont().deriveFont(Font.BOLD));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, shellEdgeColor()),
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(6, 8, 8, 8),
                        titled
                )
        ));
        return panel;
    }

    public static JPanel sidebarPanel(String title, java.awt.Component content) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        JLabel header = new JLabel(title);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setBorder(new EmptyBorder(8, 10, 4, 8));
        panel.add(header, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        panel.setBorder(new MatteBorder(0, 1, 0, 0, shellEdgeColor()));
        return panel;
    }

    public static void applySectionTitleBorder(JPanel panel, String title) {
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(2, 0, 0, 0),
                title,
                TitledBorder.LEADING,
                TitledBorder.TOP
        );
        titled.setTitleFont(panel.getFont().deriveFont(Font.BOLD));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, shellEdgeColor()),
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(4, 6, 6, 6),
                        titled
                )
        ));
    }

    public static void applyPluginGalleryTile(JButton button) {
        Color bg = UIManager.getColor("Panel.background");
        Color edge = shellEdgeColor();
        button.setFocusPainted(false);
        button.setMargin(new Insets(4, 10, 4, 10));
        button.setPreferredSize(GALLERY_TILE_SIZE);
        button.setMaximumSize(GALLERY_TILE_SIZE);
        button.setMinimumSize(GALLERY_TILE_SIZE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(new LineBorder(edge, 1, true));
        button.setBackground(bg);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hoverBg = UIManager.getColor("List.selectionBackground");
        if (hoverBg == null) {
            hoverBg = new Color(220, 220, 220);
        }
        final Color hover = hoverBg;
        final Color normalBg = bg;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalBg);
            }
        });
    }

    public static void borderlessScroll(JScrollPane scroll) {
        if (scroll == null) {
            return;
        }
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    }

    public static String formatPeerListLabel(String displayName, int notificationCount, boolean activityIndicator) {
        if (notificationCount <= 0 && !activityIndicator) {
            return displayName;
        }
        StringBuilder html = new StringBuilder("<html>");
        if (activityIndicator || notificationCount > 0) {
            html.append(notificationBadgePrefix());
        }
        html.append(escapeHtml(displayName));
        if (notificationCount > 0) {
            html.append(" <span style='color:").append(NOTIFICATION_COUNT_COLOR).append("'>(")
                    .append(notificationCount).append(")</span>");
        }
        html.append("</html>");
        return html.toString();
    }

    public static String formatPeerListLabel(String displayName, int notificationCount) {
        return formatPeerListLabel(displayName, notificationCount, false);
    }

    public static String formatActionLabelWithBadge(String label, int pendingCount, boolean indicatorOnly) {
        if (pendingCount <= 0 && !indicatorOnly) {
            return label;
        }
        if (indicatorOnly) {
            return "<html>" + notificationBadgePrefix() + escapeHtml(label) + "</html>";
        }
        return formatActionLabelWithBadge(label, pendingCount);
    }

    public static String formatActionLabelWithBadge(String label, int pendingCount) {
        if (pendingCount <= 0) {
            return label;
        }
        return "<html>" + notificationBadgePrefix()
                + escapeHtml(label)
                + " <span style='color:" + NOTIFICATION_COUNT_COLOR + "'>(" + pendingCount + ")</span></html>";
    }

    private static String notificationBadgePrefix() {
        return "<span style='color:" + NOTIFICATION_DOT_COLOR + "'>●</span> ";
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;");
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        Color muted = UIManager.getColor("Label.disabledForeground");
        if (muted != null) {
            label.setForeground(muted);
        }
        return label;
    }

    public static JButton compactCloseButton(Runnable onClose) {
        return roundTabCloseButton(onClose);
    }

    public static void alignButtonToTabStrip(JButton button) {
        if (button == null) {
            return;
        }
        button.setMargin(new Insets(2, 8, 2, 8));
        button.setFocusPainted(false);
    }

    public static JButton roundTabCloseButton(Runnable onClose) {
        JButton close = new JButton("×");
        close.setFont(close.getFont().deriveFont(Font.BOLD, 12f));
        Dimension size = new Dimension(20, 20);
        close.setPreferredSize(size);
        close.setMaximumSize(size);
        close.setMinimumSize(size);
        close.setMargin(new Insets(0, 0, 0, 0));
        close.setBorder(BorderFactory.createEmptyBorder());
        close.setBorderPainted(true);
        close.setContentAreaFilled(true);
        close.setFocusPainted(false);
        close.setOpaque(true);
        close.setBackground(UIManager.getColor("Panel.background"));
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.setToolTipText("Fechar aba");
        close.addActionListener(e -> onClose.run());
        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                close.setForeground(Color.WHITE);
                close.setBackground(CLOSE_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                close.setForeground(UIManager.getColor("Label.foreground"));
                close.setBackground(UIManager.getColor("Panel.background"));
            }
        });
        return close;
    }
}
