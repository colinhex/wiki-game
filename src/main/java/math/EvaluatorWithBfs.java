package math;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import gpt.prompts.Arguments;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mongodb.DocumentGrabber;
import neo4j.EmbeddedNeo4j;
import neo4j.EmbeddedNeo4jConfig;
import neo4j.algorithms.AlgorithmType;
import org.apache.shiro.crypto.hash.Hash;
import org.eclipse.jetty.util.IO;
import org.neo4j.cypher.internal.expressions.In;
import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanderBuilder;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import scala.collection.immutable.Stream;
import wikigame.WikiGame;

public class EvaluatorWithBfs {


    EmbeddedNeo4j embeddedNeo4j = EmbeddedNeo4j.getInstance();
    DocumentGrabber documentGrabber;

    public EvaluatorWithBfs() {
        documentGrabber = new DocumentGrabber();
        run();
    }


    public void run() {

        List<WikiGame> wikiGames = documentGrabber.getDocumentStream();

        List<WikiGame> gptGames = wikiGames.stream()
                .filter( wikiGame -> wikiGame.getWikiGameConfig().getWikiGameType().getAlgorithm().equals( AlgorithmType.GPT ) )
                .toList();

        System.out.println("size" + gptGames.size());

        Set<String> nodeSet = new HashSet<>();

        gptGames.stream().filter( gptGame -> gptGame.getPath().size() >= 7 ).forEach( gptGame -> {
            gptGame.getPath().stream().map( Arguments::getLink ).forEach( nodeSet::add );
        } );

        System.out.println("SizeOfZero: " + nodeSet.size());
        HashMap<String, Integer> distanceToGoal = new HashMap<>();

//        try {
//            java.nio.file.Path path = Paths.get( "./", "distance_goals_bt.json" );
//            String json = new ObjectMapper().writer().writeValueAsString( distanceToGoal );
//            Files.write( path, json.getBytes() );
//        } catch ( IOException ioException ) {
//            ioException.printStackTrace();
//        }
//
//        int i = 0;
//        for ( String pathNodeTitle : nodeSet) {
//            System.out.println( i++ );
//
//            try ( Transaction tx = embeddedNeo4j.getService().beginTx() ) {
//                Node pathNode = tx.findNode( Label.label( EmbeddedNeo4jConfig.nodeType ), EmbeddedNeo4jConfig.titleProperty , pathNodeTitle );
//                Node goalNode = tx.findNode( Label.label( EmbeddedNeo4jConfig.nodeType ), EmbeddedNeo4jConfig.titleProperty , "New_York_Yankees" );
//
//                TraversalDescription td = tx.traversalDescription()
//                        .breadthFirst()
//                        .relationships( RelType.LINKS_TO, Direction.OUTGOING )
//                        .uniqueness( Uniqueness.NODE_GLOBAL )
//                        .evaluator( Evaluators.includeWhereEndNodeIs( goalNode ) );
//
//                Traverser traverser = td.traverse( pathNode );
//
//                Iterator<Path> pathIterator = traverser.iterator();
//
//                if (pathIterator.hasNext()) {
//                    long c = StreamSupport.stream( pathIterator.next().nodes().spliterator(), false ).count();
//                    distanceToGoal.put( pathNodeTitle, (int)c );
//                }
//
//            }
//        }

        List<List<Arguments>> relativeDistancesSuccess = gptGames
                .stream()
                .filter( gptGame -> gptGame.getPath().size() >= 7 && gptGame.getGoalReached() != null && gptGame.getGoalReached() )
                .map( gptGame ->
                        gptGame.getPath()
                                .stream()
                                .toList()
                ).toList();

        List<List<Arguments>> relativeDistancesFailures = gptGames
                .stream()
                .filter( gptGame -> gptGame.getPath().size() >= 7 && gptGame.getGoalReached() != null && ! gptGame.getGoalReached() )
                .map( gptGame ->
                        gptGame.getPath()
                                .stream()
                                .toList()
                ).toList();


        relativeDistancesFailures.forEach( xs -> {
            System.out.print( "\\langle\\ " );
            xs.forEach( arg -> {
                System.out.print( "\\textit{"+arg.getLink()+"},\\ " );
            } );
            System.out.print( "\\rangle%n" );
            xs.forEach( arg -> {
                System.out.println( arg.getReason() );
            } );
        } );

    }


}
