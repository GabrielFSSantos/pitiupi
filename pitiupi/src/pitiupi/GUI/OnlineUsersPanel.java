package pitiupi.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import pitiupi.control.PeerInfo;
import pitiupi.control.PeerListener;
/**
 *
 * @author tony
 */
public class OnlineUsersPanel extends JPanel implements PeerListener {

    private final DefaultListModel<String> listModel;
    private final JList<String> userList;

    public OnlineUsersPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Online"));
        setPreferredSize(new Dimension(160, 0));

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        add(new JScrollPane(userList), BorderLayout.CENTER);
    }

    @Override
    public void onPeersChanged(List<PeerInfo> peers) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (PeerInfo peer : peers) {
                listModel.addElement(peer.toString());
            }
        });
    }
}