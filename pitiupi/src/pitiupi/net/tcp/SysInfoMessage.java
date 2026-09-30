package pitiupi.net.tcp;

import pitiupi.net.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SysInfoMessage extends Message {

    private final SysInfoType type;
    private final String message;

    public SysInfoMessage(String message, SysInfoType type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public SysInfoType getType() {
        return type;
    }

    public static void sendSystemInfo(TcpConnection connection, SysInfoType type, String description) {
        try {
            SysInfoMessage error = new SysInfoMessage(description, type);
            connection.send(error.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(SysInfoMessage.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    public static SysInfoMessage parseSystemInfo(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            Object object = ois.readObject();
            if (object instanceof SysInfoMessage) {
                return (SysInfoMessage) object;
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SysInfoMessage.class.getName()).log(Level.WARNING, null, ex);
        }
        return null;
    }
}
