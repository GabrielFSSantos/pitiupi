package pitiupi.net.tcp;

/**
 * O aviso de conexão na tela só aparece quando ainda não há socket aberto para o IP.
 */
public final class TcpConnectNotice {

    private TcpConnectNotice() {
    }

    public static boolean shouldShowConnecting(boolean liveSocketExists) {
        return !liveSocketExists;
    }

    public static String failureMessage(String endpoint) {
        return "Não foi possível conectar em " + endpoint
                + ". Verifique se o outro Pitiupi está aberto nessa porta e se o firewall libera TCP.";
    }
}
