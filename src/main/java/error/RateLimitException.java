package error;

public class RateLimitException extends InvalidResponseException {
    private final long waitTime;

    public RateLimitException( long waitTime ) {
        super( "", null, null );
        this.waitTime = waitTime;
    }

    public long getWaitTime() {
        return waitTime;
    }

}
