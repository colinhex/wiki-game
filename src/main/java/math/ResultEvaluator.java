package math;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import math.WikiGameStatistics.WikiGameCostDisplay;
import math.WikiGameStatistics.WikiGameCostStatistics;
import mongodb.DocumentGrabber;
import neo4j.algorithms.AlgorithmType;
import utils.JsonUtils;
import wikigame.WikiGame;

public class ResultEvaluator {


    DocumentGrabber documentGrabber;

    public ResultEvaluator() {
        documentGrabber = new DocumentGrabber();
        run();
    }


    public void run() {

        List<WikiGame> wikiGames = documentGrabber.getDocumentStream();


        List<WikiGame> gptGames = wikiGames.stream()
                .filter( wikiGame -> wikiGame.getWikiGameConfig().getWikiGameType().getAlgorithm().equals( AlgorithmType.GPT ) )
                .toList();

        writeStats( gptGames );

    }


    private List<WikiGame> filterOut( List<WikiGame> wikiGames, int bound, boolean cot ) {
        return wikiGames.stream().filter( wikiGame -> wikiGame.getWikiGameConfig().getCot() == cot && wikiGame.getWikiGameConfig().getBound() == bound ).toList();
    }

    private Predicate<SingleGameStat> dudFilter() {
        return stat -> !( stat.hallucinations() == 0 && stat.pathLength() == 0 && !stat.randomized());
    }

    private record SingleGameStat(
            String description,
            boolean success,
            int hallucinations,
            int pathLength,
            boolean randomized,
            int missed,
            int promptTokens,

            int responseTokens,

            int backTrackingSteps,

            int pruned
    ) {}

    private void writeStats( List<WikiGame> wikiGames ) {

        System.out.println( JsonUtils.toJson( wikiGames.get( 0 ), true ) );

        final WikiGameCostStatistics wikiGameCostStatistics = WikiGameCostStatistics.create();
        write( wikiGames.stream().peek( wikiGameCostStatistics::add ).map( wikiGame -> new SingleGameStat(
                (wikiGame.getWikiGameConfig().getCot() ? "CoT@":"Shot@") + wikiGame.getWikiGameConfig().getBound(),
                !wikiGame.getPath().isEmpty() && wikiGame.getPath().getLast().getLink().equals( wikiGame.getWikiGameConfig().getGoalState() ),
                wikiGame.getHallucinations() == null ? 0:wikiGame.getHallucinations(),
                wikiGame.getPathLength() == null ? 0: wikiGame.getPathLength(),
                ! (wikiGame.getWikiGameConfig().getRandomize() == null),
                wikiGame.getMissedGoalState() == null ? 0: wikiGame.getMissedGoalState(),
                wikiGame.inTokens(),
                wikiGame.outTokens(),
                wikiGame.getBacktrackingSteps() == null ? 0: wikiGame.getBacktrackingSteps(),
                wikiGame.getPruned() == null ? 0: wikiGame.getPruned().size()
        )).filter( dudFilter() ) );

        WikiGameCostDisplay wikiGameCostDisplay = WikiGameCostDisplay.with( wikiGameCostStatistics );
        wikiGameCostDisplay.render();
    }

    private static final String CSV_HEADER = "id,success,hallucinations,length,randomized,missed,prompt";
    private static final String CSV_ROW = "%s,%s,%s,%s,%s,%s,%s";

    private static final String CSV_HEADER_BT = "id,success,hallucinations,length,randomized,missed,prompt,backtrackingSteps,pruned";
    private static final String CSV_ROW_BT = "%s,%s,%s,%s,%s,%s,%s,%s,%s";

    private void write( Stream<SingleGameStat> singleGameStatStream ) {

        try {
            Path outFile = Path.of( "./out.csv" );
            Files.deleteIfExists( outFile );
            Files.createFile( outFile );

            Files.write( outFile, List.of( CSV_HEADER_BT ), StandardOpenOption.APPEND );
            Files.write( outFile, singleGameStatStream.map( stat -> String.format(
                    CSV_ROW_BT,
                    stat.description,
                    stat.success,
                    stat.hallucinations,
                    stat.pathLength,
                    stat.randomized,
                    stat.missed,
                    stat.promptTokens ,
                    stat.backTrackingSteps,
                    stat.pruned
            ) ).toList(), StandardOpenOption.APPEND );

        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

    }


}
