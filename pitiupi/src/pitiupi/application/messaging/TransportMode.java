package pitiupi.application.messaging;

/**
 * Transportes expostos na UI didática dos plugins.
 * {@link #describeForUi(boolean)} é o resumo didático ao lado do destino;
 * {@link #getLabel()} e {@link #toString()} são os nomes curtos no combo (UDP unicast, TCP, …).
 */
public enum TransportMode {
    MULTICAST_UDP("UDP multicast"),
    UNICAST_UDP("UDP unicast"),
    TCP("TCP");

    private final String transcriptTag;

    TransportMode(String transcriptTag) {
        this.transcriptTag = transcriptTag;
    }

    /** Tag no histórico (ex.: {@code [UDP multicast]}). */
    public String getLabel() {
        return transcriptTag;
    }

    /**
     * Texto do combo e resumos — linguagem simples com o termo técnico entre parênteses.
     *
     * @param everyoneInRoom {@code true} quando o destino é a sala (todos); {@code false} para um IP.
     */
    public String describeForUi(boolean everyoneInRoom) {
        return switch (this) {
            case MULTICAST_UDP -> everyoneInRoom
                    ? "Todo mundo na sala de uma vez (UDP multicast)"
                    : "Para todos de uma vez (UDP multicast)";
            case UNICAST_UDP -> everyoneInRoom
                    ? "Uma cópia para cada pessoa na sala (UDP unicast)"
                    : "Direto para o IP da pessoa (UDP unicast)";
            case TCP -> everyoneInRoom
                    ? "Uma cópia por conexão para cada pessoa na sala (TCP)"
                    : "Direto para o IP por conexão (TCP)";
        };
    }

    @Override
    public String toString() {
        return transcriptTag;
    }
}
