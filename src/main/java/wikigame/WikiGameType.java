package wikigame;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import neo4j.algorithms.AlgorithmType;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public enum WikiGameType implements Serializable {

    GPT35_R100_D5(AlgorithmType.GPT, 3),
    SP_R50_D4( AlgorithmType.SIMPLE_PATHS, 4 ),
    GPT_35_R100_D10( AlgorithmType.GPT, 10),
    SP_R100_D4(AlgorithmType.SIMPLE_PATHS, 6);

    @JsonProperty("algorithm")
    private final AlgorithmType algorithmType;

    @JsonProperty("maxDepth")
    private final Integer maxDepth;

    public AlgorithmType getAlgorithm() {
        return algorithmType;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

}
