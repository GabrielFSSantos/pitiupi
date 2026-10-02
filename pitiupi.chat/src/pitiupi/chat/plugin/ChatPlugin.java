/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.chat.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pitiupi.chat.GUI.ChatMenu;
import pitiupi.chat.GUI.ChatWindow;
import pitiupi.chat.net.ChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.models.Menus.MenuCategory;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;

/**
 *
 * @author flavio
 */
public class ChatPlugin extends Plugin {

    private ChatWindow chatWindow;

    @Override
    public void createPlugin(ConnectionManager connectionManager, GuiManager guiManager) {
        this.chatWindow = new ChatWindow(connectionManager);
        guiManager.createMenu(MenuCategory.Social, new ChatMenu(chatWindow));
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(ChatMessage.class);
    }


    @Override
    public void receiveMessage(Message message) {
        if (message instanceof ChatMessage msg) {
            this.chatWindow.receiveChatMessage(msg);
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


}
