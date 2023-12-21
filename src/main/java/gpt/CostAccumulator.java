package gpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.Usage;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
public class CostAccumulator implements Serializable {
    @JsonProperty("model")
    String model;
    @JsonProperty("promptTokens")
    int promptTokens;
    @JsonProperty("responseTokens")
    int responseTokens;
    @JsonProperty("totalTokens")
    int totalTokens;

    public CostAccumulator(String model) {
        this.model = model;
        this.promptTokens = 0;
        this.responseTokens = 0;
        this.totalTokens = 0;
    }

    public void add( Usage usage ) {
        promptTokens += usage.getPromptTokens();
        responseTokens += usage.getCompletionTokens();
        totalTokens += usage.getTotalTokens();
    }

}
