package pitiupi.control;

import pitiupi.GUI.MainWindow;

import javax.swing.*;

public class GuiManager {
    private MainWindow mainWindow;

    public GuiManager(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void addMenu(JMenu menu) {
        mainWindow.addMenu(menu);
    }
}
