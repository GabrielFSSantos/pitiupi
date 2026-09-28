package plugin;

import GUI.SocketTestMenu;
import GUI.SocketTestWindow;
import net.TestMessage;
import pitiupi.GUI.MainWindow;
import pitiupi.net.Message;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.plugin.PrivateConnectionPlugin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class SocketTest implements PrivateConnectionPlugin{
    SocketTestWindow socketTestWindow;

    @Override
    public String getName() {
        return "Test Socket";
    }

    @Override
    public String getAuthor() {
        return "Gustavo Henrique";
    }

    @Override
    public String getVersion() {
        return "0";
    }

    @Override
    public String getIdentifier() {
        return "Socket.Test.123456";
    }

    @Override
    public void createPlugin(MainWindow mainWindow) {
        this.socketTestWindow = new SocketTestWindow(mainWindow);
        mainWindow.addMenu(new SocketTestMenu(socketTestWindow));
    }

    @Override
    public Message getMessage() {
        return new TestMessage();
    }

    @Override
    public void receiveMessage(byte[] message) {
        ByteArrayInputStream bis = new ByteArrayInputStream(message);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bis);
            if (ois.readObject() instanceof TestMessage msg) {
                this.socketTestWindow.receiveMessage(msg);
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    @Override
    public void receivePrivateConnection(TcpConnection tcpConnection) {

    }

    @Override
    public void receiveBytes(TcpConnection connection, byte[] bytes) {

    }

    @Override
    public void onSocketClosed(TcpConnection tcpConnection) {

    }
}
