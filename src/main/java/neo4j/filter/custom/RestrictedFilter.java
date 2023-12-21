package neo4j.filter.custom;

import java.util.Set;
import java.util.function.Predicate;
import math.SetSupplier;
import neo4j.EmbeddedNeo4jConfig;
import org.neo4j.graphdb.Node;

public class RestrictedFilter implements Predicate<Node> {

    Set<String> set = SetSupplier.availableNodes( "Basel", "New_York_Yankees" );

    @Override
    public boolean test( Node node ) {
        return set.contains( node.getProperty( EmbeddedNeo4jConfig.titleProperty ).toString() );
    }

}
