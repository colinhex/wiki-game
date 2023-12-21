package neo4j.filter.custom;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.neo4j.graphdb.Node;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GptDuplicateFilter implements Predicate<Node> {
    String suggestedId;
    Set<String> uniqueIds;

    public static GptDuplicateFilter create() {
        return new GptDuplicateFilter( null, new HashSet<>() );
    }

    @Override
    public boolean test( Node node ) {
        suggestedId = node.getElementId();
        if ( uniqueIds.contains( suggestedId ) ) {
            return false;
        }
        uniqueIds.add( suggestedId );
        return true;
    }

}
