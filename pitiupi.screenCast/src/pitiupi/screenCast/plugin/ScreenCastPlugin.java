/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.screenCast.plugin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import pitiupi.GUI.MainWindow;
import pitiupi.net.Message;
import pitiupi.plugin.Plugin;
import pitiupi.screenCast.GUI.ScreenCastMenu;
import pitiupi.screenCast.GUI.ScreenCastWindow;
import pitiupi.screenCast.net.ScreenCastMessage;

/**
 *
 * @author flavio
 */
public class ScreenCastPlugin implements Plugin {

    private ScreenCastWindow screenCastWindow;
    private MainWindow mainWindow;

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
    public void createPlugin(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.screenCastWindow = new ScreenCastWindow(mainWindow);
        this.mainWindow.addMenu(new ScreenCastMenu(screenCastWindow));
    }

    @Override
    public void receiveMessage(byte[] message) {
        ByteArrayInputStream bis = new ByteArrayInputStream(message);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bis);
            if (ois.readObject() instanceof ScreenCastMessage msg) {
                this.screenCastWindow.receiveScreenCastMessage(msg);
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    @Override
    public Message getMessage() {
        return new ScreenCastMessage();
    }

}
