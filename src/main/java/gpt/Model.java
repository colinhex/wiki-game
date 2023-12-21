package gpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum Model implements Serializable {
    GPT_3("gpt-3"),
    GPT_35("gpt-3.5-turbo-1106"),
    GPT_4("gpt-4");

    @JsonProperty("modelIdentifier")
    private final String modelIdentifier;

    Model( String modelIdentifier ) {
        this.modelIdentifier = modelIdentifier;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

}
