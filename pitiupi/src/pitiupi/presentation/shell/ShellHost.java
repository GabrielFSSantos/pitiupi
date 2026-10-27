package pitiupi.presentation.shell;

import pitiupi.control.ConnectionManager;

/**
 * Operações da janela principal expostas à barra de menus.
 */
public interface ShellHost {

    void exit();

    void setUserName(String userName);

    ConnectionManager getConnectionManager();

    void setStatusMessage(String message);
}
