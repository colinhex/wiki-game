import java.util.List;
import java.util.stream.Collectors;
import neo4j.EmbeddedNeo4j;
import neo4j.EmbeddedNeo4jConfig;
import neo4j.algorithms.components.NodeFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

public class EmbeddedNeo4jTests {

    private static EmbeddedNeo4j embeddedNeo4j;
    private static GraphDatabaseService graphDatabaseService;

    @BeforeAll
    static void initializeDatabase() {
        embeddedNeo4j = EmbeddedNeo4j.getInstance();
        graphDatabaseService = embeddedNeo4j.getService();
    }

    @Test
    void serviceIsAvailable() {
        assert graphDatabaseService.isAvailable();
    }

    @Test
    void canQuery() {
        String actual = "Switzerland";
        String retrieved;
        long startMillis = System.currentTimeMillis();
        try ( Transaction tx = graphDatabaseService.beginTx() ) {
            retrieved = tx.findNode( Label.label("Node"), "title", actual ).getProperty( "title" ).toString();
        }
        long endMillis = System.currentTimeMillis();
        System.out.println("Query completed in " + (endMillis - startMillis) + "ms");
        Assertions.assertEquals( retrieved, actual );
    }

    @Test
    void speedTests() {
        long startMillis;
        long endMillis;
        try ( Transaction tx = graphDatabaseService.beginTx() ) {
            startMillis = System.nanoTime();
            Node node = tx.findNode( Label.label("Node"), "title", "Hulu" );
            endMillis = System.nanoTime();
            System.out.println("Retrieved node in " + (endMillis - startMillis) + "ns");

            startMillis = System.nanoTime();

            int k = node.getDegree( Direction.OUTGOING );

            endMillis = System.nanoTime();
            System.out.println("Retrieved degree in " + (endMillis - startMillis) + "ns");
            System.out.println("Degree was " + k );


        }
    }

    @Test
    void retrieveWeirdos() {
        List<String> weirdos = List.of("The_New_York_Times", "Warner_Music_Group", "Toronto_Blue_Jays", "University_of_York", "The_New_York_Times");

        for (String weirdo : weirdos) {
            try ( Transaction tx = graphDatabaseService.beginTx() ) {
                Node node = tx.findNode(
                        Label.label( EmbeddedNeo4jConfig.nodeType ),
                        EmbeddedNeo4jConfig.titleProperty,
                        weirdo
                );
                System.out.println("Found Node");
                List<Relationship> relationships = node.getRelationships( Direction.OUTGOING ).stream().toList();
                System.out.println("Relationships: " + relationships.stream().map( relationship -> relationship.getEndNode().getProperty( EmbeddedNeo4jConfig.titleProperty ) ).toList() );
            }
        }

    }




}
