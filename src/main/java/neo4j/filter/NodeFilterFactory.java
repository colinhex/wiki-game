package neo4j.filter;

import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.graphdb.Node;

@Slf4j
public abstract class NodeFilterFactory {
    public static Predicate<Node> createNodeFilter( NodeFilterType nodeFilterType ) {
        switch ( nodeFilterType ) {
            case IDENTITY -> {
                return identity();
            }
            case DUPLICATE_FILTER -> {
                return duplicateFilter();
            }
            case MARK_FILTER -> {
                return markFilter();
            }
            default -> {
                Exception e = new IllegalArgumentException("No known filter for type: " + nodeFilterType);
                log.error( "Error", e );
            }
        }
        throw new RuntimeException("Error in game thread.");
    }

    private static Predicate<Node> identity() {
        return node -> true;
    }

    private static Predicate<Node> duplicateFilter() {
        return node -> true;
    }


    private static Predicate<Node> markFilter() {
        return node -> node.getProperty( "marked" ).toString().equals( "true" );
    }

}
