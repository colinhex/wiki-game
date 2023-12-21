package cli;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;

public class CommandLineArguments {
    Boolean gpt = false;
    Boolean control = false;
    Boolean evaluate = true;
    String initialState = "Basel";
    String goalState = "New_York_Yankees";
    Integer nrOfRunsEach = 5;
    Boolean backtrack = true;
    Boolean randomize = true;


    enum CLI {
        GPT("--gpt"),
        CTRL("--ctrl"),
        EVAL("--eval"),
        INIT("--init"),
        GOAL("--goal"),
        REPEAT("--repeat"),
        BACKTRACK("--backtrack"),
        RND("--rnd"),
        SAVE("--save");

        @Getter
        private final String arg;

        CLI(String arg) {
            this.arg = arg;
        }

    }


    public CommandLineArguments( String[] args ) {
        System.out.println("Parsing command line arguments...");
        if (args.length == 0) {
            throw new IllegalArgumentException( Arrays.toString( args ) );
        }
        for (int i = 0; i < args.length; i++) {


        }
    }


    public boolean isGptEnabled() {
        return gpt;
    }

    public boolean isControlEnabled() {
        return control;
    }

    public boolean isEvaluationEnabled() {
        return evaluate;
    }

    public String getInitialState() {
        return initialState;
    }

    public String getGoalState() {
        return goalState;
    }

    public boolean isNrOfRunsSet() {
        return nrOfRunsEach != null;
    }

    public int getNrOfRuns() {
        return nrOfRunsEach;
    }

    public boolean isBacktrackingEnabled() {
        return backtrack;
    }

    public boolean isRandomizationEnabled() {
        return randomize;
    }


}
