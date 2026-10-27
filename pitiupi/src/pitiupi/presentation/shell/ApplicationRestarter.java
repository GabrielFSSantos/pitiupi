package pitiupi.presentation.shell;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reinício da aplicação após mutação de plugins (D11).
 */
public final class ApplicationRestarter {

    private ApplicationRestarter() {
    }

    public static void promptRestartIfNeeded(java.awt.Component parent) {
        int choice = JOptionPane.showConfirmDialog(
                parent,
                "Para aplicar a instalação ou remoção, reinicie o Pitiupi.\nReiniciar agora?",
                "Reinício necessário",
                JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            restart();
        }
    }

    public static void restart() {
        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + "/bin/java";
            List<String> cmd = new ArrayList<>();
            cmd.add(javaBin);
            for (String arg : ManagementArgs.currentJvmArgs()) {
                cmd.add(arg);
            }
            cmd.add("-jar");
            String jar = ManagementArgs.locateRunningJar();
            if (jar == null) {
                JOptionPane.showMessageDialog(null,
                        "Feche e abra o Pitiupi novamente (reinício automático indisponível neste modo).");
                System.exit(0);
                return;
            }
            cmd.add(jar);
            new ProcessBuilder(cmd).inheritIO().start();
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível reiniciar automaticamente. Feche e abra o Pitiupi novamente.");
        }
    }

    private static final class ManagementArgs {
        private static List<String> currentJvmArgs() {
            return List.copyOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments());
        }

        private static String locateRunningJar() {
            return pitiupi.Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        }
    }
}
