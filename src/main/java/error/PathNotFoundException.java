package error;

import wikigame.WikiGame;

public class PathNotFoundException extends AlgorithmException {

    private static final String TEMPLATE = "No path was found from state %s to %s with game type %s.";

    public PathNotFoundException( String message ) {
        super( message );
    }

    public static PathNotFoundException of( WikiGame wikiGame ) {
        return new PathNotFoundException( String.format(
                TEMPLATE,
                wikiGame.getWikiGameConfig().getInitialState(),
                wikiGame.getWikiGameConfig().getGoalState(),
                wikiGame.getWikiGameConfig().getWikiGameType().name()
        ) );
    }

}
