/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.screenCast.plugin;

import java.util.List;

import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.models.Menus.MenuCategory;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import pitiupi.screenCast.GUI.ScreenCastMenu;
import pitiupi.screenCast.GUI.ScreenCastWindow;
import pitiupi.screenCast.net.ScreenCastMessage;

/**
 *
 * @author flavio
 */
public class ScreenCastPlugin extends Plugin {

    private ScreenCastWindow screenCastWindow;

    @Override
    public String getName() {
        return "ScreenCast";
    }

    @Override
    public String getAuthor() {
        return "Flávio Schiavoni";
    }

    @Override
    public String getVersion() {
        return "0.0";
    }

    @Override
    public String getIdentifier() {
        return "Pitiupi.ScreenCast.Example";
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, GuiManager guiManager) {
        this.screenCastWindow = new ScreenCastWindow(connectionManager, guiManager);
        guiManager.createMenu(MenuCategory.Streaming, new ScreenCastMenu(screenCastWindow));
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(ScreenCastMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof ScreenCastMessage msg) {
            this.screenCastWindow.receiveScreenCastMessage(msg);
        }
    }
}
