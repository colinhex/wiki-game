package error;

public class GPTException extends AlgorithmException {


    public GPTException( String message ) {
        super( message );
    }


    public GPTException( String message, Throwable throwable ) {
        super( message, throwable );
    }


    public GPTException( Throwable throwable ) {
        super( throwable );
    }


    public GPTException( String message, Throwable throwable, boolean enableSuppression, boolean writableStacktrace ) {
        super( message, throwable, enableSuppression, writableStacktrace );
    }

    public static GPTException noArguments() {
        return new GPTException( "GPT did not return any arguments." );
    }

}
