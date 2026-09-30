/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.screenCast.GUI;

import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.screenCast.control.AlarmClock;
import pitiupi.screenCast.control.ImageTransformation;
import pitiupi.screenCast.control.ScreenShot;
import pitiupi.screenCast.net.ScreenCastMessage;
import java.awt.AWTException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import pitiupi.GUI.MainWindow;
import pitiupi.net.Message;

/**
 *
 * @author flavio
 */
public class ScreenCastWindow extends javax.swing.JFrame {

    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JCheckBox onlineButton;

    private AlarmClock alarmClock;
    private Timer timer;
    public final static int TIME = 1000;

    private float compressRatio;
    private final ConnectionManager connectionManager;
    private final GuiManager guiManager;

    public ScreenCastWindow(ConnectionManager connectionManager, GuiManager guiManager) {
        this.connectionManager = connectionManager;
        this.guiManager = guiManager;

        this.compressRatio = 0.5f;
        this.initComponents();
    }

    private void initComponents() {
        this.setTitle("screen cast");

        onlineButton = new javax.swing.JCheckBox("Sharing?");
        onlineButton.setSelected(false);
        onlineButton.addActionListener(this::onlineButtonActionPerformed);
        this.getContentPane().add(onlineButton, java.awt.BorderLayout.NORTH);

        jScrollPane1 = new javax.swing.JScrollPane();
        tabbedPane = new javax.swing.JTabbedPane();
        jScrollPane1.setViewportView(tabbedPane);
        this.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        this.pack();

        this.timer = new Timer();
        this.setSize(new java.awt.Dimension(1024, 768));
    }

    public void shareMySheet(boolean status) {
        if (status) {
            this.timer = new Timer();
            this.alarmClock = new AlarmClock(this);
            this.timer.scheduleAtFixedRate(this.alarmClock, 0, ScreenCastWindow.TIME);
        } else {
            this.timer.cancel();
        }
    }

    public void closeCurrentTab() {
        if (tabbedPane.getSelectedIndex() > 0) {
            tabbedPane.remove(tabbedPane.getSelectedIndex());
        }
    }

    public void exit() {
        this.shareMySheet(false);
    }

    public void sendScreenCastMessage() {
        byte[] msg = null;
        try {
            BufferedImage image = ScreenShot.takeAShot();
            BufferedImage rescaled = ImageTransformation.resize(image, 1024, 768);
            byte[] byteImage = ImageTransformation.compress(rescaled, compressRatio);
            ScreenCastMessage message = new ScreenCastMessage(this.connectionManager.getUserName(), byteImage);

            if (!connectionManager.sendUdpMulticast(message)) this.compressRatio -= 0.1f;
        } catch (AWTException | IOException ex) {
            Logger.getLogger(AlarmClock.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void receiveScreenCastMessage(ScreenCastMessage message) {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getImageByte());
        BufferedImage image;
        try {

            image = ImageIO.read(bais);
            ImageIcon imageIcon = new ImageIcon(image);

            JLabel myShoot = new JLabel(imageIcon);

            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                String title = tabbedPane.getTitleAt(i);
                if (title.equals(message.getName())) {
                    tabbedPane.setComponentAt(i, myShoot);
                    return;
                }
            }
            tabbedPane.addTab(message.getName(), myShoot);
        } catch (IOException ex) {
            Logger.getLogger(ScreenCastWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void onlineButtonActionPerformed(ActionEvent ae) {
        this.shareMySheet(this.onlineButton.isSelected());
    }
}
