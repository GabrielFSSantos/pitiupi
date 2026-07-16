/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.chat.plugin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import pitiupi.GUI.MainWindow;
import pitiupi.chat.GUI.ChatMenu;
import pitiupi.chat.GUI.ChatWindow;
import pitiupi.chat.net.ChatMessage;
import pitiupi.net.Message;
import pitiupi.plugin.Plugin;

/**
 *
 * @author flavio
 */
public class ChatPlugin implements Plugin {

    private ChatWindow chatWindow;
    private MainWindow mainWindow;

    public ChatPlugin() {
    }

    @Override
    public void createPlugin(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.chatWindow = new ChatWindow(mainWindow);
        this.mainWindow.addMenu(new ChatMenu(chatWindow));
    }

    @Override
    public void receiveMessage(byte[] message) {
        ByteArrayInputStream bis = new ByteArrayInputStream(message);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bis);
            if (ois.readObject() instanceof ChatMessage msg) {
                this.chatWindow.receiveChatMessage(msg);
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    @Override
    public String getName() {
        return "Chat";
    }

    @Override
    public String getAuthor() {
        return "Flávio Schiavoni";
    }

    @Override
    public String getVersion() {
        return "0";
    }

    @Override
    public Message getMessage() {
        return new ChatMessage();
    }
}
