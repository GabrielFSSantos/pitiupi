/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi;

import pitiupi.platform.LegacyPluginMigration;
import pitiupi.platform.PitiupiHome;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author flavio
 * @author tony
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    PitiupiHome.get().ensureLayout();
                    LegacyPluginMigration.maybeMigrateFromLegacyCwd();
                } catch (Exception e) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Falha ao preparar PITIUPI_HOME", e);
                }

                pitiupi.presentation.shell.LoginDialog login = new pitiupi.presentation.shell.LoginDialog();
                if (!login.showLogin()) {
                    return;
                }

                pitiupi.presentation.shell.MainShellFrame shell =
                        new pitiupi.presentation.shell.MainShellFrame(login.getUserName(), login.getPort());
                shell.showOnScreen();
            }   
        });
    }

}
