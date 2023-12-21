package neo4j.algorithms;

import error.AlgorithmException;
import error.GPTException;
import error.InvalidResponseException;
import gpt.GptServiceResponse;
import gpt.prompts.WikiGameUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import neo4j.algorithms.components.NodeFinder;
import neo4j.algorithms.components.SuccessorFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import wikigame.WikiGame;

@Getter
@Slf4j
public class GPT extends Algorithm {
    protected GPT( WikiGame wikiGame ) throws AlgorithmException {
        super( wikiGame );
    }

    @Override
    public void run( WikiGame wikiGame ) throws AlgorithmException {
        log.debug( "Running GPT algorithm." );

        wikiGame.setPath( new LinkedList<>() );
        wikiGame.setChoices( new LinkedList<>() );
        wikiGame.setGoalReached( false );
        // Main Algorithm
        while ( wikiGame.getCurrentDepth() < wikiGame.getWikiGameConfig().getMaxDepth() ) {
            wikiGame.debugPrintState();
            if ( step( wikiGame ) ) {
                wikiGame.stepDown();
                if ( wikiGame.getState( State.GPT_CURRENT ).equals( wikiGame.getState( State.GOAL ) ) ) {
                    log.info( "Reached Goal." );
                    wikiGame.setGoalReached( true );
                    break;
                }
            }
        }
        if ( ! wikiGame.getGoalReached() ) {
            log.info( "Ran out of depth." );
        }
    }

    private boolean step( WikiGame wikiGame ) throws AlgorithmException {
        log.debug( "Executing step." );
        State state = wikiGame.getPath().isEmpty() ? State.INITIAL : State.GPT_CURRENT;
        // Retrieve Successors
        String currentState;
        List<String> successors;
        try ( Transaction tx = getEmbeddedNeo4j().getService().beginTx() ) {

            // Find the node for the current state
            Node current = NodeFinder.find( wikiGame, tx, state ).getNode();
            currentState = current.getProperty( "title" ).toString();
            successors = SuccessorFinder.find( current, getRelFilter(), getNodeFilter() ).getSuccessors();

        } catch ( Exception e ) {
            throw new AlgorithmException( "Exception during successor retrieval ", e );
        }

        try {
            if ( ! getGptArguments( wikiGame, currentState, successors ) ) {
                throw GPTException.noArguments();
            }
        } catch ( InvalidResponseException e ) {
            log.warn( "Service threw exception, retrying", e );
            return false;
        }

        // Signal successful step
        return true;
    }


    private boolean getGptArguments( WikiGame wikiGame, String state, List<String> successors ) throws AlgorithmException {
        Optional<GptServiceResponse> serviceResponse = getGptService().choose( state, successors );

        if (serviceResponse.isPresent()) {
            WikiGameUtils.trackPathData( wikiGame, serviceResponse.get() );
            return true;
        }
        return false;
    }


    public static void execute( WikiGame wikiGame ) throws AlgorithmException {
        log.debug( "Executing GPT algorithm..." );
        new GPT( wikiGame );
    }

}
