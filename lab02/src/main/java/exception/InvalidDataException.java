package exception;

/**
 * Wyjątek na błędne dane wejściowe
 */
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
