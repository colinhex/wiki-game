package wikigame;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class WikiGames {
    private static int apiCalls = 0;
    private static int completedGames = 0;
    private static int totalGames = 0;

    public static void callApi() {
        apiCalls++;
    }

    private static class ClockThread implements Runnable {
        static int clockMillis = 500;
        static ClockThread clockThread;
        static Thread thread;

        private boolean stop;

        public static void runInstance() {
            clockThread = new ClockThread();
            thread = new Thread( clockThread );
            thread.setDaemon( true );
            thread.start();
        }

        public static void stopInstance() {
            try {
                clockThread.stopThread();
                Thread.sleep( clockMillis + 50 );
            } catch ( InterruptedException e ) {
                throw new RuntimeException( e );
            }
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }

        public ClockThread() {
            this.stop = false;
        }

        public void stopThread() {
            this.stop = true;
        }

        @Override
        public void run() {
            try {
                while ( ! stop ) {
                    if (apiCalls > 3) {
                        System.out.print("\r API CALLS: " + apiCalls + " GAME " + completedGames + "/" + totalGames + " ".repeat( 6 ));
                        System.out.flush();
                    }
                    Thread.sleep( clockMillis );
                }
            } catch ( InterruptedException interruptedException ) {
                interruptedException.printStackTrace();
                log.debug("Clock Thread stopped.");
            }
        }

    }


    public static void runAllGptTestsSimple( int repeats, int nrOfRunsEach, String initialState, String goalState ) {
        runAllGptTests( repeats, nrOfRunsEach, initialState, goalState, false, false );
    }

    public static void runAllGptTests( int repeats, int nrOfRunsEach, String initialState, String goalState, boolean randomize, boolean backtrack ) {

        totalGames = repeats * nrOfRunsEach * 11;
        for (int k = 0; k < repeats; k++) {
            System.out.println("------------------ RUNNING ALL GPT TESTS --------------------- REPEAT " + k );
            System.out.println("------------------ OFF THE SHELF, 0-SHOT ~ REPEAT " + k );

            zeroShot( nrOfRunsEach, initialState, goalState, randomize, backtrack );

            for (int i = 1; i < 6; i++) {
                System.out.println("------------------ "+i+"-SHOT ~ REPEAT " + k );
                nShot( nrOfRunsEach, initialState, goalState, i, randomize, backtrack );
            }

            for (int i = 1; i < 6; i++) {
                System.out.println("------------------ CoT@"+i+" ~ REPEAT " + k );
                nCoT( nrOfRunsEach, initialState, goalState, i, randomize, backtrack );
            }
        }

    }

    public static void zeroShot( int numberOfRuns, String initialState, String goalState, boolean randomize, boolean backtrack ) {
        WikiGameOrchestrator.setMaxConcurrentGames( numberOfRuns );
        WikiGameOrchestrator wikiGameOrchestrator = WikiGameOrchestrator.renewedInstance();
        WikiGameConfiguration wikiGameConfig = WikiGameConfigurationFactory.create(
                WikiGameType.GPT_35_R100_D10,
                initialState,
                goalState,
                0,
                false,
                randomize,
                backtrack
        );
        ClockThread.runInstance();
        for (int i = 0; i < numberOfRuns; i++) {
            wikiGameOrchestrator.launchGame( wikiGameConfig );
        }
        wikiGameOrchestrator.handleResults( 1 );
        completedGames += numberOfRuns;
        ClockThread.stopInstance();
    }

    public static void simpleNShot( int numberOfRuns, String initialState, String goalState, int n ) {
        withPromptEngineering( numberOfRuns, initialState, goalState, n, false, false, false );
    }

    public static void simpleNCoT( int numberOfRuns, String initialState, String goalState, int n ) {
        withPromptEngineering( numberOfRuns, initialState, goalState, n, true, false, false );
    }

    public static void nShot( int numberOfRuns, String initialState, String goalState, int n, boolean randomize, boolean backtrack ) {
        withPromptEngineering( numberOfRuns, initialState, goalState, n, false, randomize, backtrack );
    }

    public static void nCoT( int numberOfRuns, String initialState, String goalState, int n, boolean randomize, boolean backtrack ) {
        withPromptEngineering( numberOfRuns, initialState, goalState, n, true, randomize, backtrack );
    }

    public static void withPromptEngineering( int numberOfRuns, String initialState, String goalState, int examples, boolean cot, boolean randomize, boolean backtrack ) {
        WikiGameOrchestrator.setMaxConcurrentGames( numberOfRuns );
        WikiGameOrchestrator wikiGameOrchestrator = WikiGameOrchestrator.renewedInstance();
        WikiGameConfiguration wikiGameConfig = WikiGameConfigurationFactory.create(
                WikiGameType.GPT_35_R100_D10,
                initialState,
                goalState,
                examples,
                cot,
                randomize,
                backtrack
        );
        ClockThread.runInstance();
        for (int i = 0; i < numberOfRuns; i++) {
            wikiGameOrchestrator.launchGame( wikiGameConfig );
        }
        wikiGameOrchestrator.handleResults( 1 );
        completedGames += numberOfRuns;
        ClockThread.stopInstance();
    }

    public static void runControl() {
        WikiGameOrchestrator.setMaxConcurrentGames( 1 );
        WikiGameOrchestrator wikiGameOrchestrator = WikiGameOrchestrator.renewedInstance();

        WikiGameConfiguration wikiGameConfig = WikiGameConfigurationFactory.create(
                WikiGameType.SP_R100_D4, "Basel", "New_York_Yankees", null, null, false, false
        );
        wikiGameOrchestrator.launchGame( wikiGameConfig );
        wikiGameOrchestrator.handleResults( 1 );
    }
}
