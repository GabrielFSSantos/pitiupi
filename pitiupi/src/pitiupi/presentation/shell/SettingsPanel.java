package pitiupi.presentation.shell;

import pitiupi.control.ConnectionManager;
import pitiupi.errors.PitiupiException;
import pitiupi.models.Plugin;
import pitiupi.plugins.store.InstalledPluginRef;
import pitiupi.plugins.store.PluginStore;
import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configurações: perfil de rede e gestão de JARs.
 */
public final class SettingsPanel extends JPanel {

    public interface NetworkSettingsListener {
        void onIdentityApplied(String userName, int port);
    }

    private final PluginStore store;
    private final ConnectionManager connectionManager;
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"Nome", "ID", "Arquivo", "Versão"}, 0);
    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField();
    private final JSpinner portSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(5000, 1, 65535, 1));
    private final JComboBox<String> egressCombo = new JComboBox<>(new String[]{"Automático", "Cabo", "Wi-Fi"});
    private final JSpinner ttlSpinner = new JSpinner(
            new javax.swing.SpinnerNumberModel(NetworkPreferences.DEFAULT_TTL, NetworkPreferences.MIN_TTL, NetworkPreferences.MAX_TTL, 1));
    private final JLabel interfaceLabel = new JLabel(" ");
    private final JTextField ipField = new JTextField();
    private final JTextField instanceField = new JTextField();
    private final JLabel peersLabel = new JLabel();
    private final Timer refreshTimer;
    private boolean applying;
    private NetworkSettingsListener settingsListener = (name, port) -> {
    };
    private Map<String, Plugin> loadedByIdentifier = Map.of();

    public SettingsPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.store = new PluginStore();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        for (JTextField field : List.of(ipField, instanceField)) {
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

    public void setSettingsListener(NetworkSettingsListener listener) {
        this.settingsListener = listener != null ? listener : (name, port) -> {
        };
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
        addRow(panel, gbc, 1, "Porta", portSpinner);
        addRow(panel, gbc, 2, "Saída", egressCombo);
        addRow(panel, gbc, 3, "Interface", interfaceLabel);
        addRow(panel, gbc, 4, "TTL", ttlSpinner);
        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(ShellUi.mutedLabel("1 alcança só a rede local."), gbc);
        addRow(panel, gbc, 6, "IP local", ipField);
        addRow(panel, gbc, 7, "ID desta instância", instanceField);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Peers visíveis:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(peersLabel, gbc);

        JButton apply = new JButton("Aplicar");
        apply.setFocusPainted(false);
        apply.addActionListener(e -> applyNetworkSettings());
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(apply, gbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
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

    private void applyNetworkSettings() {
        applying = true;
        try {
            commitSpinner(portSpinner);
            commitSpinner(ttlSpinner);
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe um nome na rede.");
                return;
            }
            int port = (Integer) portSpinner.getValue();
            int ttl = NetworkPreferences.clampTtl((Integer) ttlSpinner.getValue());
            LocalNetworkInfo.EgressMode mode = egressFromCombo();
            boolean nameChanged = !name.equals(connectionManager.getUserName());
            boolean portChanged = port != connectionManager.getPort();
            boolean egressChanged = mode != LocalNetworkInfo.getEgressMode();
            boolean ttlChanged = ttl != connectionManager.getMulticastTtl();
            if (!nameChanged && !portChanged && !egressChanged && !ttlChanged) {
                return;
            }
            if (portChanged || egressChanged || ttlChanged) {
                String error = connectionManager.applyNetwork(port, ttl, mode);
                if (error != null) {
                    JOptionPane.showMessageDialog(this, error, "Configuração de rede", JOptionPane.WARNING_MESSAGE);
                    refreshNetworkInfo();
                    return;
                }
            }
            NetworkPreferences.saveName(name);
            NetworkPreferences.savePort(connectionManager.getPort());
            NetworkPreferences.saveTtl(connectionManager.getMulticastTtl());
            NetworkPreferences.saveEgress(LocalNetworkInfo.getEgressMode());
            settingsListener.onIdentityApplied(name, connectionManager.getPort());
            JOptionPane.showMessageDialog(
                    this,
                    confirmation(nameChanged, portChanged, connectionManager.getPort(), egressChanged || ttlChanged)
            );
        } finally {
            applying = false;
            refreshNetworkInfo();
        }
    }

    private static void commitSpinner(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException ignored) {
            // mantém o último valor válido do modelo
        }
    }

    private LocalNetworkInfo.EgressMode egressFromCombo() {
        Object selected = egressCombo.getSelectedItem();
        if ("Cabo".equals(selected)) {
            return LocalNetworkInfo.EgressMode.WIRED;
        }
        if ("Wi-Fi".equals(selected)) {
            return LocalNetworkInfo.EgressMode.WIRELESS;
        }
        return LocalNetworkInfo.EgressMode.AUTO;
    }

    private static String confirmation(boolean nameChanged, boolean portChanged, int port, boolean egressOrTtlChanged) {
        StringBuilder text = new StringBuilder();
        if (portChanged) {
            text.append("Porta alterada para ")
                    .append(port)
                    .append(". Você saiu da sala anterior e passou a procurar colegas na porta ")
                    .append(port)
                    .append('.');
        } else if (egressOrTtlChanged) {
            text.append("Saída de rede atualizada. A descoberta usa esta interface.");
        }
        if (nameChanged) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append("Nome atualizado. Os colegas veem o novo nome no próximo anúncio (até 5 s).");
        }
        return text.toString();
    }

    private void refreshNetworkInfo() {
        if (!applying && !isEditingNetworkFields()) {
            nameField.setText(connectionManager.getUserName());
            portSpinner.setValue(connectionManager.getPort());
            ttlSpinner.setValue(connectionManager.getMulticastTtl());
            egressCombo.setSelectedItem(comboLabel(LocalNetworkInfo.getEgressMode()));
        }
        interfaceLabel.setText(LocalNetworkInfo.describeActiveInterface());
        ipField.setText(LocalNetworkInfo.bestLocalIpv4());
        instanceField.setText(connectionManager.getHeartbeatManager().getInstanceId());
        int peers = Math.max(0, connectionManager.getPresenceService().listPeerViews().size() - 1);
        peersLabel.setText(peers + " (além de você)");
    }

    private static String comboLabel(LocalNetworkInfo.EgressMode mode) {
        if (mode == LocalNetworkInfo.EgressMode.WIRED) {
            return "Cabo";
        }
        if (mode == LocalNetworkInfo.EgressMode.WIRELESS) {
            return "Wi-Fi";
        }
        return "Automático";
    }

    private boolean isEditingNetworkFields() {
        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focus == null) {
            return false;
        }
        return SwingUtilities.isDescendingFrom(focus, nameField)
                || SwingUtilities.isDescendingFrom(focus, portSpinner)
                || SwingUtilities.isDescendingFrom(focus, ttlSpinner)
                || SwingUtilities.isDescendingFrom(focus, egressCombo);
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
