package plugin;

import GUI.SocketTestMenu;
import GUI.SocketTestWindow;
import net.TestMessage;
import pitiupi.GUI.MainWindow;
import pitiupi.net.Message;
import pitiupi.plugin.Plugin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class SocketTest implements Plugin{
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
    public void createPlugin(MainWindow mainWindow) {
        this.socketTestWindow = new SocketTestWindow(mainWindow);
        mainWindow.addMenu(new SocketTestMenu(socketTestWindow));
    }

    @Override
    public Message getMessage() {
        return new TestMessage();
    }

    @Override
    public void receiveMessage(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
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
}
