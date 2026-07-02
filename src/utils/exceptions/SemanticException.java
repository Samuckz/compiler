package utils.exceptions;

public class SemanticException extends RuntimeException {
    private final int line;

    public SemanticException(String message, int line) {
        super("Erro semântico na linha " + line + ": " + message);
        this.line = line;
    }

    public int getLine() { return line; }
}
