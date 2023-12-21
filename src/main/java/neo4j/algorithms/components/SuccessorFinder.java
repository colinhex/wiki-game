package neo4j.algorithms.components;

import error.AlgorithmException;
import error.SuccessorsNotFoundException;
import java.util.List;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import neo4j.EmbeddedNeo4jConfig;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SuccessorFinder extends MeasurableBlock {
    @Getter
    private final Node currentState;
    @Getter
    private final Predicate<Relationship> relFilter;
    @Getter
    private final Predicate<Node> nodeFilter;

    @Setter
    @Getter
    private List<String> successors;

    public static SuccessorFinder find( final Node currentState , final Predicate<Relationship> relFilter, final Predicate<Node> nodeFilter ) throws AlgorithmException {
        return new SuccessorFinder( currentState, relFilter, nodeFilter, null ).execute();
    }

    public SuccessorFinder execute() throws AlgorithmException {
        return (SuccessorFinder) super.execute();
    }

    @Override
    public void run() throws AlgorithmException {
        List<String> successors = getCurrentState().getRelationships( Direction.OUTGOING )
                .stream()
                .filter( getRelFilter() )
                .map( Relationship::getEndNode )
                .filter( getNodeFilter() )
                .map( node -> node.getProperty( EmbeddedNeo4jConfig.titleProperty ).toString() )
                .toList();
        if ( successors.isEmpty() ) {
            throw SuccessorsNotFoundException.create( getCurrentState().getProperty( EmbeddedNeo4jConfig.titleProperty ).toString() );
        }
        setSuccessors( successors );
    }

}
