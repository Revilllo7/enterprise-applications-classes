package exception;

/**
 * Błędy związane z komunikacją z API / deserializacją.
 */
public class ApiException extends Exception {
    public ApiException(String message) { super(message); }
    public ApiException(String message, Throwable cause) { super(message, cause); }
}
