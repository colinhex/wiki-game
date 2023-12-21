package wikigame;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import error.AlgorithmException;
import gpt.CostAccumulator;
import gpt.prompts.Arguments;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import neo4j.algorithms.Algorithm;
import neo4j.algorithms.AlgorithmType;
import neo4j.algorithms.State;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
@JsonInclude(Include.NON_NULL)
public class WikiGame implements Runnable, Serializable {
    @JsonProperty("id")
    private String id;
    @Setter
    @JsonProperty("startTimestamp")
    private String timestamp;
    @JsonProperty("wikiGameConfig")
    private WikiGameConfiguration wikiGameConfig;
    @Setter
    @JsonProperty("completed")
    private Boolean completed;
    @Setter
    @JsonProperty("error")
    private Throwable error;
    @Setter
    @JsonProperty("pathLength")
    private Integer pathLength;
    @Setter
    @JsonProperty("goalReached")
    private Boolean goalReached;
    @Setter
    @JsonProperty("arguments")
    private LinkedList<Arguments> path;
    @Setter
    @JsonProperty("completeArguments")
    private LinkedList<String> completeArguments;
    @Setter
    @JsonProperty("choices")
    private LinkedList<List<String>> choices;
    @Setter
    @JsonProperty("costs")
    private CostAccumulator costs;
    @Setter
    @JsonProperty("runtimeMillis")
    private Long runtimeMillis;
    @Setter
    @JsonProperty("numberOfResultPaths")
    private Integer numberOfResultPaths;
    @Setter
    @JsonProperty("numberOfTraversedRelationships")
    private Long numberOfTraversedRelationships;
    @Setter
    @JsonProperty("numberOfTraversedNodes")
    private Long numberOfTraversedNodes;
    @Setter
    @JsonProperty("traversalResult")
    private List<List<String>> traversalResult;
    @Setter
    @JsonProperty("hallucinations")
    private Integer hallucinations;
    @Setter
    @JsonProperty("backtrackingDepth")
    private Integer backtrackingDepth;
    @Setter
    @JsonProperty("backtrackingSteps")
    private Integer backtrackingSteps;
    @Setter
    @JsonProperty("currentDepth")
    private Integer currentDepth;
    @Setter
    @JsonProperty("pruned")
    private Set<String> pruned;
    @Setter
    @JsonProperty("missedGoalState")
    private Integer missedGoalState;

    private WikiGame( String id, WikiGameConfiguration wikiGameConfig ) {
        this.id = id;
        this.wikiGameConfig = wikiGameConfig;
        this.currentDepth = 0;
        this.backtrackingSteps = 0;
        this.hallucinations = 0;
        this.missedGoalState = 0;
        this.pruned = new HashSet<>();
        this.path = new LinkedList<>();
        this.completeArguments = new LinkedList<>();
        this.choices = new LinkedList<>();
    }

    public static Pair<String, WikiGame> create( WikiGameConfiguration wikiGameConfig ) {
        String uuid = UUID.randomUUID().toString();
        WikiGame wikiGame = new WikiGame( uuid, wikiGameConfig );
        wikiGame.setCompleted( false );
        return Pair.of( uuid, wikiGame );
    }

    @Override
    public void run() {
        setTimestamp( new Timestamp( System.currentTimeMillis() ).toString() );
        Algorithm.runAlgorithm( this );
    }

    private void verifyAccess( State state ) throws AlgorithmException {
        if ( state == State.INITIAL || state == State.GOAL ) {
            return;
        }
        if ( ! getWikiGameConfig().getWikiGameType().getAlgorithm().equals( AlgorithmType.GPT ) ) {
            throw AlgorithmException.illegalStateAccess( this, state );
        }
    }

    public String getState( State state ) throws AlgorithmException {
        verifyAccess( state );
        try {
            return switch ( state ) {
                case INITIAL -> getWikiGameConfig().getInitialState();
                case GOAL -> getWikiGameConfig().getGoalState();
                case GPT_CURRENT -> getPath().getLast().getLink();
                case GPT_PREVIOUS -> getPath().get( getPath().size() - 2 ).getLink();
            };
        } catch ( Exception e ) {
            throw new AlgorithmException( "Exception accessing WikiGame state(s) ", e );
        }

    }


    public void hallucinate() {
        hallucinations++;
    }

    public void missGoalState() {
        missedGoalState++;
    }

    public void backtrack( int backtrackingSteps ) {
        this.backtrackingSteps += backtrackingSteps;
    }

    public void stepDown() {
        currentDepth++;
    }

    private String getDepthAndStateAndChoices(int k) {
        return String.format( "%s.) \t S[ %s ] \t\t Next: %s...", k, getPath().get( k ).getLink(), getChoices().get( k ).stream().sorted().toList().subList( 0, 5 ) );
    }


    private String getDepthAndState(int k) {
        return String.format( "%s.) \t S[ %s ] ", k, getPath().get( k ).getLink() );
    }

    public void debugPrintState() {
        if ( ! pathIsEmpty() && log.isDebugEnabled() ) {
            log.debug("-".repeat( 100 ));
            log.debug("Pruned: " + getPruned());
            for (int k = 0; k < getPath().size(); k++) {
                if ( getChoices().size() > k ) {
                    log.debug(getDepthAndStateAndChoices( k ));
                } else {
                    log.debug(getDepthAndState( k ));
                }
            }
            log.debug("-".repeat( 100 ));
        }
    }

    public boolean pathIsEmpty() {
        return getPath().isEmpty();
    }


    public int getNodeExpansions() {
        return getPath().size() - 1 + getBacktrackingSteps();
    }

    public int getPrunedNodes() {
        return getPruned().size();
    }

    public int totalTokens() {
        return getCosts().getTotalTokens();
    }

    public int inTokens() {
        return getCosts().getPromptTokens();
    }

    public int outTokens() {
        return getCosts().getResponseTokens();
    }


}
