package wikigame;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import gpt.Model;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import neo4j.filter.NodeFilterType;
import neo4j.filter.RelFilterType;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@Getter
public class WikiGameConfiguration implements Serializable {
    @JsonProperty("wikiGameType")
    private WikiGameType wikiGameType;
    @JsonProperty("nodeFilterTypes")
    private List<NodeFilterType> nodeFilterTypes;
    @JsonProperty("relFilterTypes")
    private List<RelFilterType> relFilterTypes;
    @JsonProperty("model")
    private Model model;
    @JsonProperty("maxDepth")
    private int maxDepth;
    @JsonProperty("initialState")
    private String initialState;
    @JsonProperty("goalState")
    private String goalState;
    @JsonProperty("marker")
    private String marker;
    @JsonProperty("examples")
    private Integer bound;
    @JsonProperty("chain-of-thought")
    private Boolean cot;
    @JsonProperty("backtracking")
    private Boolean backtracking;
    @JsonProperty("randomize")
    private Boolean randomize;

    public static class ConfigurationBuilder {
        private WikiGameType wikiGameType;
        private final List<NodeFilterType> nodeFilterTypes;
        private final List<RelFilterType> relFilterTypes;
        private Model modelType;
        private Integer maxDepth;
        private String initialState;
        private String goalState;
        private String marker;
        private Integer bound;
        private Boolean cot = false;

        private Boolean backtracking = false;

        private Boolean randomize = false;

        private ConfigurationBuilder() {
            this.nodeFilterTypes = new LinkedList<>();
            this.relFilterTypes = new LinkedList<>();
        }

        public ConfigurationBuilder setGame( WikiGameType game ) {
            wikiGameType = game;
            return this;
        }

        public ConfigurationBuilder addFilterType( NodeFilterType nodeFilter ) {
            nodeFilterTypes.add( nodeFilter );
            return this;
        }

        public ConfigurationBuilder addFilterType( RelFilterType relFilter ) {
            relFilterTypes.add( relFilter );
            return this;
        }

        public ConfigurationBuilder setModel( Model model ) {
            modelType = model;
            return this;
        }

        public ConfigurationBuilder setMaxDepth( int depth ) {
            maxDepth = depth;
            return this;
        }


        public ConfigurationBuilder setInitial( String initial ) {
            initialState = initial;
            return this;
        }


        public ConfigurationBuilder setGoal( String goal ) {
            goalState = goal;
            return this;
        }

        public ConfigurationBuilder setMarker( String nodeMarker ) {
            marker = nodeMarker;
            return this;
        }

        public ConfigurationBuilder setCot( boolean chainOfThought ) {
            cot = chainOfThought;
            return this;
        }

        public ConfigurationBuilder setExamples( int nrExamples ) {
            bound = nrExamples;
            return this;
        }

        public ConfigurationBuilder setBacktracking( Boolean backtracking ) {
            this.backtracking = backtracking;
            return this;
        }

        public ConfigurationBuilder setRandomize( Boolean randomize ) {
            this.randomize = randomize;
            return this;
        }


        public WikiGameConfiguration build() {
            return new WikiGameConfiguration(
                    wikiGameType,
                    nodeFilterTypes,
                    relFilterTypes,
                    modelType,
                    maxDepth,
                    initialState,
                    goalState,
                    marker,
                    bound,
                    cot,
                    backtracking,
                    randomize
            );
        }
    }


    public static ConfigurationBuilder builder() {
        return new ConfigurationBuilder();
    }


}
