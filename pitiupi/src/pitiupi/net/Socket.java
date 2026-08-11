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
 * Responsável pela comunicação multicast entre a aplicação e os plugins.
 *
 * <p>Esta classe gerencia o envio e o recebimento de mensagens, além de
 * manter a conexão com o grupo multicast utilizado pela aplicação.</p>
 *
 * @author flavio
 * @author tony
 */
public class Socket extends Thread {

    private MulticastSocket multicastSocket;
    private InetAddress address;
    private MainWindow main;
    private volatile boolean running = true;
    public final static String INET_ADDR = "224.0.0.3";

    /**
     * Cria e inicializa a conexão multicast utilizada pela aplicação.
     *
     * @param main janela principal da aplicação.
     */
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

    /**
     * Envia uma mensagem para o grupo multicast da aplicação.
     *
     * @param msg mensagem serializada a ser enviada.
     * @throws IOException caso ocorra um erro durante o envio da mensagem.
     */
    public void send(byte[] msg) throws IOException {
        DatagramPacket msgPacket;
        msgPacket = new DatagramPacket(msg, msg.length, this.address, this.main.getPort());
        multicastSocket.send(msgPacket);
    }

    public void close() {
        running = false;
        if (multicastSocket != null) {
            try {
                multicastSocket.leaveGroup(address);
            } catch (IOException ex) {

            }
            multicastSocket.close();
        }
    }


    /**
     * Inicia o loop de recepção de mensagens da aplicação.
     *
     * <p>As mensagens recebidas são verificadas inicialmente para identificar
     * mensagens de heartbeat. Caso não sejam heartbeats, seu conteúdo é
     * encaminhado para todos os plugins carregados, que decidem se devem ou
     * não processá-lo.</p>
     *
     * @throws IOException caso ocorra um erro durante a recepção da mensagem.
     * @throws ClassNotFoundException caso a desserialização de uma mensagem falhe.
     */
    public void receive() throws UnknownHostException, IOException, ClassNotFoundException {
        byte[] buf = new byte[256000];
        while (running) {
            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
            try {
                multicastSocket.receive(msgPacket);
            } catch (IOException ex) {
                if (!running) {
                    break;
                }
                throw ex;
            }
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

    /**
     * Tenta desserializar os dados recebidos como uma {@code HeartbeatMessage}.
     *
     * @param data dados serializados da mensagem.
     * @return a mensagem desserializada caso os dados representem uma
     * {@code HeartbeatMessage}; caso contrário, {@code null}.
     */
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
