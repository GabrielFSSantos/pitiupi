package pitiupi.plugin;

import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.models.Menus.Menu;
import pitiupi.models.Menus.MenuCategory;
import pitiupi.models.Messages.Message;
import pitiupi.net.tcp.TcpConnection;

import java.net.InetAddress;

public class Pitiupi {

    private final ConnectionManager connectionManager;
    private final GuiManager guiManager;

    public Pitiupi(ConnectionManager connectionManager, GuiManager guiManager) {
        this.connectionManager = connectionManager;
        this.guiManager = guiManager;
    }

    public boolean sendUdpMulticast(Message message) {
        return connectionManager.sendUdpMulticast(message);
    }

    public boolean sendUdpUnicast(Message message, InetAddress address) {
        return connectionManager.sendUdpUnicast(message, address);
    }

    public boolean sendTcp(Message message, InetAddress address) {
        return connectionManager.sendTcp(message, address);
    }

    public TcpConnection connectTcp(InetAddress address, String senderId, String receiverId) {
        return connectionManager.connectPluginTcp(address, senderId, receiverId);
    }

    public String getUserName() {
        return connectionManager.getUserName();
    }

    public int getPort() {
        return connectionManager.getPort();
    }

    public void createMenu(MenuCategory category, Menu menu) {
        guiManager.createMenu(category, menu);
    }
}
