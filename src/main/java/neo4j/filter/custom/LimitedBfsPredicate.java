package neo4j.filter.custom;

import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.graphdb.Relationship;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LimitedBfsPredicate implements Predicate<Relationship>, EntityCounter {
    @Setter
    private int limit;
    @Getter(AccessLevel.PUBLIC)
    private long counter = 0;
    private int limitCounter;
    @Setter
    private int originId;

    public static LimitedBfsPredicate create( final int limit ) {
        LimitedBfsPredicate limitedBfsPredicate = new LimitedBfsPredicate();
        limitedBfsPredicate.setLimit( limit );
        limitedBfsPredicate.setOriginId( -1 );
        return limitedBfsPredicate;
    }

    @Override
    public boolean test( Relationship relationship ) {
        int nodeId = (int) relationship.getStartNode().getId();

        if ( originId == -1 || nodeId != originId ) {
            originId = nodeId;
            limitCounter = 0;
        }

        boolean ruling = limitCounter++ < limit;

        if (ruling) {
            counter++;
        }

        return ruling;
    }

}
