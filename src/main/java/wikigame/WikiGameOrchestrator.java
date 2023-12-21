package wikigame;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import math.ControlEvaluator;
import mongodb.DocumentSaver;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Getter
public class WikiGameOrchestrator {
    @Setter
    private static boolean saveResults = true;
    @Setter
    private static boolean simplePathsFlag = false;

    @Setter
    private static String controlUuid;
    @Setter
    private static int maxConcurrentGames = 1;
    private static WikiGameOrchestrator instance;

    @Setter
    private boolean isShutDown;

    public static WikiGameOrchestrator getInstance() {
        if (instance == null) {
            instance = new WikiGameOrchestrator( maxConcurrentGames );
        }
        return instance;
    }

    /**
     * Testing
     */
    public static WikiGameOrchestrator renewedInstance() {
        instance = new WikiGameOrchestrator( maxConcurrentGames );
        return instance;
    }

    private final ExecutorService executorService;

    private final HashMap<String, WikiGame> wikiGames;

    private WikiGameOrchestrator(int concurrentGames) {
        executorService = Executors.newFixedThreadPool( concurrentGames );
        wikiGames = new HashMap<>();
        isShutDown = false;
    }

    public String launchGame( WikiGameConfiguration wikiGameConfig ) {
        if (isShutDown()) {
            log.debug("Can not launch games, this instance is shut down.");
            return null;
        }
        Pair<String, WikiGame> pair = WikiGame.create( wikiGameConfig );
        String uuid = pair.getLeft();
        WikiGame wikiGame = pair.getRight();

        getWikiGames().put( uuid, wikiGame );
        getExecutorService().submit( wikiGame );

        if ( simplePathsFlag ) {
            setControlUuid( uuid );
        }

        return uuid;
    }


    public void handleResults( int timeoutHours ) {
        setShutDown( true );


        getExecutorService().shutdown();


        boolean success;
        try {
            log.debug("Awaiting Termination");
            success = getExecutorService().awaitTermination( timeoutHours, TimeUnit.HOURS );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }

        if (success) {
            log.debug("All results have come in.");
        } else {
            log.debug("There was a timeout.");
        }

        if (simplePathsFlag) {
            ControlEvaluator.evaluate( wikiGames.get( controlUuid ) );
        }

        if (saveResults) {
            writeResultsToDatabase();
        }
    }

    private void writeResultsToDatabase() {
        log.info( "Writing results to database..." );
        DocumentSaver documentSaver = new DocumentSaver();
        getWikiGames().keySet().stream()
                .map( getWikiGames()::get )
                .map( documentSaver::mapToJson )
                .map( documentSaver::mapToDocument )
                .forEach( documentSaver::insert );
        documentSaver.close();
    }

    public WikiGame getGameStatus( String uuid ) {
        return wikiGames.get( uuid );
    }

}
