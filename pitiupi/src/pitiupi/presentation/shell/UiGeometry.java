package pitiupi.presentation.shell;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Cálculos de tamanho/posição para diálogos e janelas.
 */
public final class UiGeometry {

    private UiGeometry() {
    }

    public static Dimension fractionOfScreen(double widthFraction, double heightFraction) {
        Rectangle bounds = usableScreenBounds();
        int w = Math.max(320, (int) (bounds.width * widthFraction));
        int h = Math.max(240, (int) (bounds.height * heightFraction));
        return new Dimension(w, h);
    }

    public static Rectangle usableScreenBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (!GraphicsEnvironment.isHeadless()) {
            Rectangle configBounds = ge.getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getBounds();
            if (configBounds.width > 0 && configBounds.height > 0) {
                return configBounds;
            }
        }
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (screen.width > 0 && screen.height > 0) {
            return new Rectangle(0, 0, screen.width, screen.height);
        }
        return ge.getMaximumWindowBounds();
    }

    public static Dimension effectiveSize(Dimension size, Dimension preferred) {
        int w = size.width > 0 ? size.width : preferred.width;
        int h = size.height > 0 ? size.height : preferred.height;
        if (w <= 0) {
            w = preferred.width > 0 ? preferred.width : 320;
        }
        if (h <= 0) {
            h = preferred.height > 0 ? preferred.height : 240;
        }
        return new Dimension(w, h);
    }

    public static Dimension effectiveWindowSize(Window window) {
        return effectiveSize(window.getSize(), window.getPreferredSize());
    }

    public static Point centerLocation(Rectangle bounds, Dimension windowSize) {
        int x = bounds.x + Math.max(0, (bounds.width - windowSize.width) / 2);
        int y = bounds.y + Math.max(0, (bounds.height - windowSize.height) / 2);
        return new Point(x, y);
    }

    public static void centerOnScreen(Window window) {
        window.setLocationByPlatform(false);
        Rectangle bounds = usableScreenBounds();
        Dimension size = effectiveWindowSize(window);
        Point location = centerLocation(bounds, size);
        window.setLocation(location);
    }

    /**
     * Recentra após o WM reposicionar a janela (comum no WSLg).
     */
    public static void installCenterOnShow(Window window) {
        window.setLocationByPlatform(false);
        WindowAdapter windowAdapter = new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                centerOnScreen(window);
            }
        };
        ComponentAdapter componentAdapter = new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (e.getComponent() == window) {
                    centerOnScreen(window);
                }
            }
        };
        window.addWindowListener(windowAdapter);
        window.addComponentListener(componentAdapter);
    }

    public static void presentWindow(Window window) {
        installCenterOnShow(window);
        centerOnScreen(window);
        window.setVisible(true);
        javax.swing.SwingUtilities.invokeLater(() -> centerOnScreen(window));
    }
}
