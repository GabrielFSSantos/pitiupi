package pitiupi.presentation.shell;

import pitiupi.control.ConnectionManager;
import pitiupi.errors.PitiupiException;
import pitiupi.models.Plugin;
import pitiupi.plugins.store.InstalledPluginRef;
import pitiupi.plugins.store.PluginStore;
import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configurações: perfil de rede e gestão de JARs.
 */
public final class SettingsPanel extends JPanel {

    private final PluginStore store;
    private final ConnectionManager connectionManager;
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"Nome", "ID", "Arquivo", "Versão"}, 0);
    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField();
    private final JTextField portField = new JTextField();
    private final JTextField ipField = new JTextField();
    private final JTextField instanceField = new JTextField();
    private final JLabel peersLabel = new JLabel();
    private final Timer refreshTimer;
    private Map<String, Plugin> loadedByIdentifier = Map.of();

    public SettingsPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.store = new PluginStore();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        for (JTextField field : List.of(nameField, portField, ipField, instanceField)) {
            field.setEditable(false);
        }

        add(buildNetworkPanel(), BorderLayout.NORTH);
        add(buildPluginsPanel(), BorderLayout.CENTER);

        refreshTimer = new Timer(5000, e -> refreshNetworkInfo());
        refreshTimer.setRepeats(true);
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                refreshNetworkInfo();
                refreshTimer.start();
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
                refreshTimer.stop();
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });

        refreshTable();
        refreshNetworkInfo();
    }

    public void setLoadedPlugins(List<Plugin> plugins) {
        Map<String, Plugin> map = new HashMap<>();
        for (Plugin p : plugins) {
            map.put(p.getIdentifier(), p);
        }
        loadedByIdentifier = map;
        refreshTable();
    }

    public void onTabSelected() {
        refreshNetworkInfo();
    }

    private JPanel buildNetworkPanel() {
        JPanel panel = ShellUi.sectionPanel("Sua conexão");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(panel, gbc, 0, "Nome na rede", nameField);
        addRow(panel, gbc, 1, "Porta", portField);
        addRow(panel, gbc, 2, "IP local", ipField);
        addRow(panel, gbc, 3, "ID desta instância", instanceField);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Peers visíveis:"), gbc);
        gbc.gridx = 1;
        panel.add(peersLabel, gbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JPanel buildPluginsPanel() {
        JPanel wrapper = ShellUi.sectionPanel("Plugins instalados");
        wrapper.setLayout(new BorderLayout(8, 8));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(400, 200));
        ShellUi.borderlessScroll(scroll);
        wrapper.add(scroll, BorderLayout.CENTER);

        JLabel help = ShellUi.mutedLabel("Depois de instalar ou remover, reinicie o Pitiupi para aplicar.");
        wrapper.add(help, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton install = new JButton("Instalar JAR…");
        JButton remove = new JButton("Remover selecionado");
        install.setFocusPainted(false);
        remove.setFocusPainted(false);
        actions.add(install);
        actions.add(remove);
        wrapper.add(actions, BorderLayout.SOUTH);

        install.addActionListener(e -> installJar());
        remove.addActionListener(e -> removeSelected());

        return wrapper;
    }

    private void refreshNetworkInfo() {
        nameField.setText(connectionManager.getUserName());
        portField.setText(String.valueOf(connectionManager.getPort()));
        ipField.setText(LocalNetworkInfo.bestLocalIpv4());
        instanceField.setText(connectionManager.getHeartbeatManager().getInstanceId());
        int peers = Math.max(0, connectionManager.getPresenceService().listPeerViews().size() - 1);
        peersLabel.setText(peers + " (além de você)");
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            List<InstalledPluginRef> refs = store.listInstalled();
            for (InstalledPluginRef ref : refs) {
                String id = ref.getIdentifier().orElse("—");
                String display = loadedByIdentifier.values().stream()
                        .filter(p -> id.equals(p.getIdentifier()))
                        .map(Plugin::getDisplayName)
                        .findFirst()
                        .orElse(ref.getDisplayName().orElse(ref.getName().orElse("—")));
                tableModel.addRow(new Object[]{
                        display,
                        id,
                        ref.getFileName(),
                        ref.getVersion().orElse("—")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível listar plugins: " + ex.getMessage());
        }
    }

    private void installJar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Plugins JAR", "jar"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            store.installFromFile(file.toPath());
            refreshTable();
            ApplicationRestarter.promptRestartIfNeeded(this);
        } catch (PitiupiException ex) {
            JOptionPane.showMessageDialog(this, ex.getUserMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Falha ao instalar: " + ex.getMessage());
        }
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        String fileName = (String) tableModel.getValueAt(row, 2);
        int ok = JOptionPane.showConfirmDialog(this, "Remover " + fileName + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Path pluginsDir = store.listInstalled().stream()
                    .filter(r -> r.getFileName().equals(fileName))
                    .map(InstalledPluginRef::getJarPath)
                    .findFirst()
                    .orElse(null);
            if (pluginsDir == null) {
                return;
            }
            store.remove(pluginsDir);
            refreshTable();
            ApplicationRestarter.promptRestartIfNeeded(this);
        } catch (PitiupiException ex) {
            JOptionPane.showMessageDialog(this, ex.getUserMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Falha ao remover: " + ex.getMessage());
        }
    }
}
