package se233.asterioddemo.exception;

public class SpriteNotFoundException extends Exception {
    public SpriteNotFoundException(String message) {
        super(message);
    }

    public SpriteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public void printStackTrace() {
        // Do nothing, suppress the stack trace globally
        System.out.println("se233.asterioddemo.exception.GameException: " + getMessage());
    }
}
