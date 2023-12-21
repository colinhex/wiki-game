package error;

import neo4j.algorithms.State;
import wikigame.WikiGame;

public class AlgorithmException extends GameException {

    protected static final String FAILURE_TEMPLATE = "During handling of exception another occurred";

    public AlgorithmException( String message ) {
        super( message );
    }


    public AlgorithmException( String message, Throwable throwable ) {
        super( message, throwable );
    }


    public AlgorithmException( Throwable throwable ) {
        super( throwable );
    }


    public AlgorithmException( String message, Throwable throwable, boolean enableSuppression, boolean writableStacktrace ) {
        super( message, throwable, enableSuppression, writableStacktrace );
    }

    public static AlgorithmException illegalStateAccess( final WikiGame wikiGame, final State state ) {
        return new AlgorithmException( String.format( "Illegal access in %s when trying to get state %s".formatted(
                wikiGame.getWikiGameConfig().getWikiGameType(),
                state.name()
        ) ) );
    }

}
