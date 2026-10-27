package pitiupi.presentation.shell;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import java.awt.Dimension;

/**
 * JTabbedPane que ocupa apenas a altura da trilha de abas (conteúdo real fica fora).
 */
final class StripOnlyTabbedPane extends JTabbedPane {

    StripOnlyTabbedPane() {
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
        putClientProperty("JTabbedPane.contentBorderInsets", new java.awt.Insets(0, 0, 0, 0));
    }

    /**
     * Cada aba precisa de um componente distinto; reutilizar o mesmo quebra o tab count do Swing.
     */
    static JComponent newHiddenPlaceholder() {
        return new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(0, 0);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(0, 0);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(0, 0);
            }
        };
    }

    @Override
    public void addTab(String title, java.awt.Component component) {
        super.addTab(title, newHiddenPlaceholder());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, stripHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, stripHeight());
    }

    int stripHeight() {
        if (getTabCount() == 0) {
            return 30;
        }
        try {
            java.awt.Rectangle bounds = getUI().getTabBounds(this, 0);
            if (bounds != null && bounds.height > 0) {
                return bounds.height + 6;
            }
        } catch (Exception ignored) {
            // headless / UI not ready
        }
        return 30;
    }
}
