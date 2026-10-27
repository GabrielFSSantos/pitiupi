package pitiupi.errors;

/**
 * Códigos de erro expostos à UI (mensagens em português).
 */
public enum ErrorCode {
    PLUGIN_INSTALL_FAILED("Não foi possível instalar o plugin."),
    PLUGIN_REMOVE_FAILED("Não foi possível remover o plugin."),
    PLUGIN_LOAD_FAILED("Falha ao carregar um plugin."),
    INVALID_PATH("Caminho de arquivo inválido.");

    private final String userMessage;

    ErrorCode(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
