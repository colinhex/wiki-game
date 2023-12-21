package gpt.prompts;

import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatMessage;
import error.AlgorithmException;
import gpt.GptServiceResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import wikigame.WikiGame;

@Slf4j
public class WikiGameUtils {


    public static void backtrack(
            final WikiGame wikiGame,
            final Context context,
            final Arguments arguments
    ) throws AlgorithmException {

        try {
            int backTrackIndex = wikiGame.getPath().indexOf( arguments );
            wikiGame.getPruned().addAll(
                wikiGame.getPath().subList( backTrackIndex + 1, wikiGame.getPath().size() )
                    .stream()
                    .map( Arguments::getLink )
                    .toList()
            );

            int backTrackingSteps = Math.abs( backTrackIndex - wikiGame.getPath().size() );
            wikiGame.setBacktrackingDepth( backTrackIndex );

            wikiGame.setPath( new LinkedList<>( wikiGame.getPath().subList( 0, backTrackIndex + 1 ) ) );
            wikiGame.backtrack( backTrackingSteps );
            wikiGame.setCurrentDepth( wikiGame.getPath().size() - 1 );

            List<ChatMessage> corrected = new LinkedList<>( context.getMessages().subList( 0, wikiGame.getWikiGameConfig().getBound() * 2 ) );
            List<String> newSuccessors = curateSuccessors( wikiGame, wikiGame.getChoices().get( backTrackIndex ), wikiGame.getPruned() );

            wikiGame.setChoices( new LinkedList<>( wikiGame.getChoices().subList( 0, backTrackIndex ) ) );

            corrected.add( Contexts.getInstructionFor( wikiGame, newSuccessors ) );

            context.setSuccessors( new HashSet<>( newSuccessors ) );
            context.setPath( new HashSet<>( wikiGame.getPath().stream().map( Arguments::getLink ).toList() ) );
            context.setMessages( corrected );
            context.setState( arguments.getLink() );

        } catch ( Exception e ) {
            e.printStackTrace();
            throw new AlgorithmException( "Error occurred during backtracking", e );
        }

        wikiGame.debugPrintState();

    }

    public static List<String> curateSuccessors( final WikiGame wikiGame, final List<String> successors, final Set<String> pruned ) {
        ArrayList<String> curated = new ArrayList<>( successors );
        curated.removeAll( pruned );
        curated.removeAll( wikiGame.getPath().stream().map( Arguments::getLink ).toList() );
        return curated;
    }

    public static Context createContext( final WikiGame wikiGame, final List<String> successors, final Set<String> pruned, final String state ) throws AlgorithmException {
        List<String> curated = curateSuccessors( wikiGame, successors, pruned );
        Pair<List<ChatMessage>, List<ChatFunctionDynamic>> requestParameters = Contexts.getFor( wikiGame, curated );
        return new Context(
                wikiGame.getWikiGameConfig().getModel().getModelIdentifier(),
                requestParameters.getLeft(),
                requestParameters.getRight(),
                curated,
                wikiGame.getPath().stream().map( Arguments::getLink ).toList(),
                state
        );
    }

    public static void trackPathData( final WikiGame wikiGame, final GptServiceResponse serviceResponse ) {
        if ( wikiGame.pathIsEmpty() ) {
            wikiGame.getPath().add( Arguments.of( serviceResponse.state(), "Initial State" ) );
        }
        wikiGame.getChoices().add( serviceResponse.successors() );
        wikiGame.getPath().add( serviceResponse.arguments() );
        wikiGame.getCosts().add( serviceResponse.usage() );
        wikiGame.getCompleteArguments().add( serviceResponse.arguments().getLink() );
    }

}
