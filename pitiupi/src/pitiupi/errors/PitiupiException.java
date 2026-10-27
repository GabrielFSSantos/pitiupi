package pitiupi.errors;

/**
 * Exceção de domínio com código para a interface.
 */
public final class PitiupiException extends Exception {

    private final ErrorCode code;

    public PitiupiException(ErrorCode code) {
        super(code.getUserMessage());
        this.code = code;
    }

    public PitiupiException(ErrorCode code, String detail) {
        super(detail);
        this.code = code;
    }

    public PitiupiException(ErrorCode code, Throwable cause) {
        super(code.getUserMessage(), cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getUserMessage() {
        return code.getUserMessage();
    }
}
