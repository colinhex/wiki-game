
import math.EvaluatorWithBfs;
import math.ResultEvaluator;
import wikigame.WikiGameOrchestrator;
import wikigame.WikiGames;

public class Main {

    public static void main( String[] args ) {
        WikiGameOrchestrator.setSaveResults( true );

        boolean gpt = false;
        boolean control = false;
        boolean evaluate = true;

        String initialState = "Remote_Work";
        String goalState = "Renaissance";

        int nrOfRunsEach = 5;
        boolean backtrack = true;
        boolean randomize = true;

        if (gpt) {
            WikiGames.runAllGptTests( 5, nrOfRunsEach, initialState, goalState, backtrack, randomize );
        }

        if (control) {
            WikiGameOrchestrator.setSaveResults( false );
            WikiGameOrchestrator.setSimplePathsFlag( true );
            WikiGames.runControl();
        }

        if (evaluate) {
            // new EvaluatorWithBfs();
            new ResultEvaluator();
        }
    }



}
