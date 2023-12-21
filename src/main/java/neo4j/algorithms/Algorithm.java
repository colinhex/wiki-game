package neo4j.algorithms;

import error.AlgorithmException;
import gpt.CostAccumulator;
import gpt.GptService;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import neo4j.EmbeddedNeo4j;
import neo4j.filter.NodeFilterFactory;
import neo4j.filter.NodeFilterType;
import neo4j.filter.RelFilterFactory;
import neo4j.filter.RelFilterType;
import neo4j.filter.custom.EntityCounter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import utils.StopWatch;
import wikigame.WikiGame;

@Slf4j
public abstract class Algorithm {
    @Getter(AccessLevel.PROTECTED)
    private final EmbeddedNeo4j embeddedNeo4j;

    @Getter(AccessLevel.PROTECTED)
    private final GptService gptService;
    @Getter(AccessLevel.PROTECTED)
    private final Predicate<Node> nodeFilter;
    @Getter(AccessLevel.PROTECTED)
    private final Predicate<Relationship> relFilter;

    @Setter(AccessLevel.PRIVATE)
    private EntityCounter nodes;
    @Setter(AccessLevel.PRIVATE)
    private EntityCounter relationships;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private StopWatch stopWatch;

    private Optional<EntityCounter> getNodes() {
        return Optional.ofNullable( nodes );
    }

    private Optional<EntityCounter> getRelationships() {
        return Optional.ofNullable( relationships );
    }

    protected Algorithm( WikiGame wikiGame ) throws AlgorithmException {
        embeddedNeo4j = EmbeddedNeo4j.getInstance();

        nodeFilter = wikiGame.getWikiGameConfig().getNodeFilterTypes()
                .stream()
                .map( NodeFilterFactory::createNodeFilter )
                .peek( filter -> {
                    if ( filter instanceof EntityCounter ) {
                        setNodes( (EntityCounter) filter );
                    }
                } )
                .reduce( NodeFilterFactory.createNodeFilter( NodeFilterType.IDENTITY ), Predicate::and );

        relFilter = wikiGame.getWikiGameConfig().getRelFilterTypes()
                .stream()
                .map( RelFilterFactory::createRelFilter )
                .peek( filter -> {
                    if ( filter instanceof EntityCounter ) {
                        setRelationships( (EntityCounter) filter );
                    }
                } )
                .reduce( RelFilterFactory.createRelFilter( RelFilterType.IDENTITY ), Predicate::and );


        if (wikiGame.getWikiGameConfig().getWikiGameType().getAlgorithm().equals( AlgorithmType.GPT )) {
            this.gptService = GptService.create( wikiGame );
            wikiGame.setCosts( new CostAccumulator( wikiGame.getWikiGameConfig().getModel().getModelIdentifier() ) );
            wikiGame.setHallucinations( 0 );
            wikiGame.setCompleteArguments( new LinkedList<>() );
        } else {
            this.gptService = null;
        }

        markStart();

        run( wikiGame );

        markEnd( wikiGame );
    }

    public static void runAlgorithm( WikiGame wikiGame ) {
        try {
            switch ( wikiGame.getWikiGameConfig().getWikiGameType().getAlgorithm() ) {
                case GPT -> {
                    GPT.execute( wikiGame );
                }
                case SIMPLE_PATHS -> {
                    SimplePaths.execute( wikiGame );
                }
                case STRING_DISTANCE -> {

                }
                default -> {
                    Exception e = new IllegalArgumentException("No such algorithm.");
                    log.error( "Error in Thread", e );
                    throw new RuntimeException();
                }
            }
        } catch ( Exception e ) {
            // wikiGame.setError( e );
            log.error( "Exception occurred during algorithm ", e );
        }
    }

    protected abstract void run( WikiGame wikiGame ) throws AlgorithmException;

    private void includeCountable( WikiGame wikiGame ) {
        getNodes().ifPresent( nodes -> wikiGame.setNumberOfTraversedNodes( nodes.getCounter() ) );
        getRelationships().ifPresent( relationships -> wikiGame.setNumberOfTraversedRelationships( relationships.getCounter() ) );
    }

    private void markStart() {
        setStopWatch( StopWatch.create() );
    }

    private void markEnd( WikiGame wikiGame ) {
        wikiGame.setCompleted( true );
        wikiGame.setRuntimeMillis( getStopWatch().measure() );
        includeCountable( wikiGame );
        switch ( wikiGame.getWikiGameConfig().getWikiGameType().getAlgorithm() ) {
            case GPT -> {
                wikiGame.setPathLength( wikiGame.getPath().size() );
                if ( ! wikiGame.getWikiGameConfig().getBacktracking()) {
                    wikiGame.setCompleteArguments( new LinkedList<>() );
                }
            }
            case SIMPLE_PATHS -> {
                wikiGame.setNumberOfResultPaths( wikiGame.getTraversalResult().size() );
            }
        }
    }

}
