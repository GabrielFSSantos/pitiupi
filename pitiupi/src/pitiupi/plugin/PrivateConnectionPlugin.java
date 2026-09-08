package pitiupi.plugin;

import pitiupi.net.tcp.TcpConnection;

public interface PrivateConnectionPlugin extends Plugin {
    public void receivePrivateConnection(TcpConnection connection);
    public void receiveBytes(byte[] bytes);
    public void onSocketClosed(TcpConnection connection);
}
