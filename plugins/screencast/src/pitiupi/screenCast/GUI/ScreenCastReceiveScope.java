package pitiupi.screenCast.GUI;

public enum ScreenCastReceiveScope {
    BROADCAST,
    DIRECT;

    public String bannerHtml() {
        if (this == BROADCAST) {
            return "<html><i>Transmissão para todos na rede</i></html>";
        }
        return "<html><i>Transmissão direcionada a você</i></html>";
    }
}
