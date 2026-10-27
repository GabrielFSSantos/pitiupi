package pitiupi.chat.control;

public interface PrivateChatHost {
    String getPluginIdentifier();

    void openConnection(String ip);
}
