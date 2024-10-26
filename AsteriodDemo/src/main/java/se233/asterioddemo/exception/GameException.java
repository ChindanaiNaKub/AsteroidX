package se233.asterioddemo.exception;

public class GameException extends RuntimeException {

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public void printStackTrace() {
        // Do nothing, suppress the stack trace globally
        System.out.println("se233.asterioddemo.exception.GameException: " + getMessage());
    }
}
