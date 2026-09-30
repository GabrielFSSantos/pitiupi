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
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.net.Message;
import pitiupi.plugin.Plugin;

/**
 *
 * @author flavio
 */
public class ChatPlugin implements Plugin {

    private ChatWindow chatWindow;

    public ChatPlugin() {
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, GuiManager guiManager) {
        this.chatWindow = new ChatWindow(connectionManager);
        guiManager.addMenu(new ChatMenu(chatWindow));
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
        } catch (IOException | ClassNotFoundException ex) {
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
    public String getIdentifier() {
        return "Pitiupi.Chat.Example";
    }

    @Override
    public Message getMessage() {
        return new ChatMessage();
    }
}
