package neo4j.filter;

import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import neo4j.filter.custom.LimitedBfsPredicate;
import neo4j.filter.custom.LimitedDfsPredicate;
import org.neo4j.graphdb.Relationship;

@Slf4j
public abstract class RelFilterFactory {

    public static Predicate<Relationship> createRelFilter( RelFilterType relFilterType ) {
        switch ( relFilterType ) {
            case IDENTITY -> {
                return identity();
            }
            case LIMITED_BFS_25 -> {
                return limitedBfs( 25 );
            }
            case LIMITED_BFS_50 -> {
                return limitedBfs( 50 );
            }
            case LIMITED_BFS_75 -> {
                return limitedBfs( 75 );
            }
            case LIMITED_BFS_100 -> {
                return limitedBfs( 100 );
            }
            case LIMITED_DFS_25 -> {
                return limitedDfs( 25 );
            }
            case LIMITED_DFS_50 -> {
                return limitedDfs( 50 );
            }
            case LIMITED_DFS_75 -> {
                return limitedDfs( 75 );
            }
            case LIMITED_DFS_100 -> {
                return limitedDfs( 100 );
            }
            case MARK_FILTER -> {
                return markFilter();
            }
            default -> {
                Exception e = new IllegalArgumentException("No known filter for type: " + relFilterType );
                log.error( "No known filter type.", e );
            }
        }
        throw new RuntimeException("Error in game thread.");
    }

    private static Predicate<Relationship> identity() {
        return rel -> true;
    }

    private static Predicate<Relationship> limitedBfs( final int limit ) {
        return LimitedBfsPredicate.create( limit );
    }

    private static Predicate<Relationship> limitedDfs( final int limit ) {
        return LimitedDfsPredicate.create( limit );
    }

    private static Predicate<Relationship> markFilter() {
        return relationship -> relationship.getProperty( "marked" ).toString().equals( "true" );
    }

}
