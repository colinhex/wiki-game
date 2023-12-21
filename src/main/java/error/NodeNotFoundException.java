package error;

import neo4j.algorithms.State;
import wikigame.WikiGame;

public class NodeNotFoundException extends AlgorithmException {


    private static final String TEMPLATE = "[%s] %s %s %s";

    private static final String GOAL_STATE = "Goal state";

    private static final String INITIAL_STATE = "Initial state";

    private static final String STATE = "State";
    private static final String MISSING = "not found in database.";

    public NodeNotFoundException( String message ) {
        super( message );
    }

    public NodeNotFoundException( String message, Throwable throwable ) {
        super( message, throwable );
    }

    private static NodeNotFoundException missingState(
            WikiGame wikiGame,
            String stateDescription,
            String stateIdentifier
    ) {
        return new NodeNotFoundException( String.format(
                TEMPLATE,
                wikiGame.getId(),
                stateDescription,
                stateIdentifier,
                MISSING
        ) );
    }

    public static NodeNotFoundException missingState( WikiGame wikiGame, State state ) {
        try {
            return switch ( state ) {
                case INITIAL -> missingState( wikiGame, GOAL_STATE, wikiGame.getWikiGameConfig().getGoalState() );
                case GOAL -> missingState( wikiGame, INITIAL_STATE, wikiGame.getWikiGameConfig().getInitialState() );
                case GPT_CURRENT ->  missingState( wikiGame, STATE, wikiGame.getPath().getLast().getLink() );
                case GPT_PREVIOUS ->missingState( wikiGame, STATE, wikiGame.getPath().get( wikiGame.getPath().size() - 2 ).getLink() );
            };
        } catch ( Exception e ) {
            throw new RuntimeException( FAILURE_TEMPLATE, new NodeNotFoundException( "Missing state: " + state.name(), e ) );
        }
    }

}
