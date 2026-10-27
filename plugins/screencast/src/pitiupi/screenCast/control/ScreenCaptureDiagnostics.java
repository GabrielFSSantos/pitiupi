package pitiupi.screenCast.control;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ScreenCaptureDiagnostics {

    private static final Logger LOG = Logger.getLogger(ScreenCaptureDiagnostics.class.getName());
    private static boolean environmentLogged;

    private ScreenCaptureDiagnostics() {
    }

    public static boolean isLikelyBlackFrame(BufferedImage image) {
        if (image == null || image.getWidth() < 8 || image.getHeight() < 8) {
            return true;
        }
        int stepX = Math.max(1, image.getWidth() / 16);
        int stepY = Math.max(1, image.getHeight() / 16);
        int dark = 0;
        int samples = 0;
        for (int y = 0; y < image.getHeight(); y += stepY) {
            for (int x = 0; x < image.getWidth(); x += stepX) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                if (r < 12 && g < 12 && b < 12) {
                    dark++;
                }
                samples++;
            }
        }
        return samples > 0 && dark >= samples * 0.92;
    }

    public static String blackFrameUserMessage() {
        logEnvironmentOnce();
        String wayland = System.getenv("WAYLAND_DISPLAY");
        String display = System.getenv("DISPLAY");
        return "<html><p><b>Captura retornou tela preta.</b></p>"
                + "<p>Em WSL/WSLg o <code>java.awt.Robot</code> costuma não capturar o desktop.</p>"
                + "<ul>"
                + "<li>Teste o Pitiupi no <b>Windows nativo</b> (fora do WSL) para compartilhar a tela real.</li>"
                + "<li>No WSL, confira WSLg: <code>WAYLAND_DISPLAY="
                + (wayland != null ? wayland : "—")
                + "</code>, <code>DISPLAY="
                + (display != null ? display : "—")
                + "</code>.</li>"
                + "<li>Se os frames ainda saem pretos, o envio na rede pode ocorrer, mas o preview local não reflete sua tela.</li>"
                + "</ul></html>";
    }

    private static void logEnvironmentOnce() {
        if (environmentLogged) {
            return;
        }
        environmentLogged = true;
        try {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            LOG.log(Level.INFO, "Screen capture env: {0}, displays={1}, DISPLAY={2}, WAYLAND_DISPLAY={3}",
                    new Object[]{
                            env.getClass().getSimpleName(),
                            env.getScreenDevices().length,
                            System.getenv("DISPLAY"),
                            System.getenv("WAYLAND_DISPLAY")
                    });
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Screen capture env unavailable", ex);
        }
    }
}
