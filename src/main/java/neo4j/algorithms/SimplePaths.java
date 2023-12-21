package neo4j.algorithms;

import error.AlgorithmException;
import error.PathNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import neo4j.EmbeddedNeo4jConfig;
import neo4j.algorithms.components.NodeFinder;
import neo4j.filter.custom.RestrictedFilter;
import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanderBuilder;
import org.neo4j.graphdb.Transaction;
import wikigame.WikiGame;

@Slf4j
public class SimplePaths extends Algorithm {

    protected SimplePaths( final WikiGame wikiGame ) throws AlgorithmException {
        super( wikiGame );
    }

    @Override
    protected void run( final WikiGame wikiGame ) throws AlgorithmException {
        try ( Transaction tx = getEmbeddedNeo4j().getService().beginTx() ) {
            Node initialState = NodeFinder.find( wikiGame, tx, State.INITIAL ).getNode();
            Node goalState = NodeFinder.find( wikiGame, tx, State.GOAL ).getNode();
            PathFinder<Path> finder = createPathFinder( wikiGame, tx );
            log.debug( "Running Simple Paths" );
            findSimplePaths( wikiGame, finder, initialState, goalState );

        } catch ( Exception e ) {
            throw new AlgorithmException( e );
        }
    }

    private PathFinder<Path> createPathFinder( final WikiGame wikiGame, final Transaction tx ) {
        return GraphAlgoFactory.allSimplePaths(
            new BasicEvaluationContext( tx, getEmbeddedNeo4j().getService() ),
            PathExpanderBuilder
                    .allTypes( Direction.OUTGOING )
                    // .addNodeFilter( new RestrictedFilter() )
                    .build(),
            wikiGame.getWikiGameConfig().getMaxDepth()
        );
    }

    private void findSimplePaths( final WikiGame wikiGame, final PathFinder<Path> finder, final Node initialState, final Node goalState ) throws AlgorithmException {
        try {
            log.debug( "Trying..." );
            Iterable<Path> iterable = finder.findAllPaths( initialState, goalState );
            List<List<String>> result = new LinkedList<>();
            StreamSupport.stream( iterable.spliterator(), false ).forEach( path -> {
                result.add( StreamSupport.stream( path.nodes().spliterator(), false )
                        .map( node -> node.getProperty( EmbeddedNeo4jConfig.titleProperty ).toString() )
                        .toList()
                );
            } );

            if ( result.isEmpty() ) {
                log.debug( "No results." );
                throw PathNotFoundException.of( wikiGame );
            }
            log.debug( "RESULT: " + result );

            wikiGame.setTraversalResult( result );
        } catch ( Exception e ) {
            throw new AlgorithmException( "Something happend", e );
        } catch ( OutOfMemoryError e ) {
            throw new AlgorithmException( "ERROR", e );
        }
    }

    public static void execute( final WikiGame wikiGame ) throws AlgorithmException {
        new SimplePaths( wikiGame );
    }

}
