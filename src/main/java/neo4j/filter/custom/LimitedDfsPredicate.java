package neo4j.filter.custom;

import java.util.BitSet;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LimitedDfsPredicate implements Predicate<Relationship>, EntityCounter {
    private static final int NODE_BIT_SET_SIZE = 7000000;
    private static final int RELATIONSHIP_BIT_SET_SIZE = 600000000;
    private final BitSet nodeCache = new BitSet( NODE_BIT_SET_SIZE );
    private final BitSet relationshipCache = new BitSet( RELATIONSHIP_BIT_SET_SIZE );
    @Setter
    private int limit;
    @Getter(AccessLevel.PUBLIC)
    private long counter = 0;

    public static LimitedDfsPredicate create( final int limit ) {
        LimitedDfsPredicate limitedDfsPredicate = new LimitedDfsPredicate();
        limitedDfsPredicate.setLimit( limit );
        return limitedDfsPredicate;
    }

    @Override
    public boolean test( Relationship relationship ) {
        Node origin = relationship.getStartNode();
        int originId = (int) origin.getId();
        int relationshipId = (int) relationship.getId();
        if ( ! nodeCache.get( originId )) {
            origin.getRelationships( Direction.OUTGOING )
                    .stream()
                    .limit( limit )
                    .mapToInt( r -> (int) r.getId() )
                    .forEach( relationshipCache::set );
            nodeCache.set( originId );
        }
        boolean ruling = relationshipCache.get( relationshipId );
        if (ruling) {
            counter++;
        }
        return ruling;
    }

}
