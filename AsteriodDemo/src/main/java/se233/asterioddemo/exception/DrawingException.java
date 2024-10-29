package se233.asterioddemo.exception;

public class DrawingException extends RuntimeException {
    public DrawingException(String message) {
        super(message);
    }

    public DrawingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public void printStackTrace() {
        // Do nothing, suppress the stack trace globally
        System.out.println("se233.asterioddemo.exception.GameException: " + getMessage());
    }
}
