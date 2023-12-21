package error;

public class GameException extends Exception {

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public GameException(Throwable throwable) {
        super(throwable);
    }

    public GameException(String message, Throwable throwable, boolean enableSuppression, boolean writableStacktrace ) {
        super(message, throwable, enableSuppression, writableStacktrace);
    }

}
