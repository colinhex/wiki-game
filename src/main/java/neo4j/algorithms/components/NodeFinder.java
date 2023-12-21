package neo4j.algorithms.components;

import error.AlgorithmException;
import error.NodeNotFoundException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import neo4j.EmbeddedNeo4jConfig;
import neo4j.algorithms.State;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import wikigame.WikiGame;

@Getter(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeFinder extends MeasurableBlock {
    private final WikiGame wikiGame;
    private final Transaction tx;
    private final State state;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PRIVATE)
    private Node node;

    public static NodeFinder find( final WikiGame wikiGame, final Transaction tx, final State state ) throws AlgorithmException {
         return new NodeFinder( wikiGame, tx, state, null ).execute();
    }

    @Override
    public void run() throws AlgorithmException {
        setNode( Optional.ofNullable( getTx().findNode(
                Label.label( EmbeddedNeo4jConfig.nodeType ),
                EmbeddedNeo4jConfig.titleProperty,
                getWikiGame().getState( getState() )
        )).orElseThrow( () -> NodeNotFoundException.missingState( getWikiGame(), getState() ) ) );
    }

    @Override
    public NodeFinder execute() throws AlgorithmException {
        return (NodeFinder) super.execute();
    }

}
