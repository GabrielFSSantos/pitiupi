/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.net;

import pitiupi.GUI.MainWindow;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pitiupi.plugin.Plugin;
import pitiupi.net.HeartbeatMessage;

/**
 *
 * @author flavio
 * @author tony
 */
public class Socket extends Thread {

    private MulticastSocket multicastSocket;
    private InetAddress address;
    private MainWindow main;
    public final static String INET_ADDR = "224.0.0.3";

    public Socket(MainWindow main) {
        this.main = main;
        try {
            this.address = InetAddress.getByName(Socket.INET_ADDR);
        } catch (UnknownHostException ex) {
            System.getLogger(Socket.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        try {
            multicastSocket = new MulticastSocket(this.main.getPort());
            multicastSocket.setSendBufferSize(256000);
            multicastSocket.setReceiveBufferSize(256000);
            multicastSocket.setReuseAddress(true);
            multicastSocket.joinGroup(address);
        } catch (IOException ex) {
            System.out.println("There is no socket connection. Sorry.");
            System.out.println(ex.toString());
        }
    }

    public void send(byte[] msg) throws IOException {
        DatagramPacket msgPacket;
        msgPacket = new DatagramPacket(msg, msg.length, this.address, this.main.getPort());
        multicastSocket.send(msgPacket);
    }
    
    public void receive() throws UnknownHostException, IOException, ClassNotFoundException {
        byte[] buf = new byte[256000];
        while (true) {
            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
            multicastSocket.receive(msgPacket);

            HeartbeatMessage heartbeat = tryParseHeartbeat(msgPacket.getData());
            if (heartbeat != null) {
                main.getHeartbeatManager().receiveHeartbeat(heartbeat, msgPacket.getAddress());
                continue;
            }

            for (Plugin plugin : main.getPlugins()) {
                System.out.println(msgPacket.getAddress().toString());
                plugin.receiveMessage(msgPacket.getData());
            }
        }
    }
     
    private HeartbeatMessage tryParseHeartbeat(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInput in = new ObjectInputStream(bais);
            Object obj = in.readObject();
            return (obj instanceof HeartbeatMessage) ? (HeartbeatMessage) obj : null;
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            return null; 
        }
    }

    @Override
    public void run() {
        try {
            this.receive();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Socket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
