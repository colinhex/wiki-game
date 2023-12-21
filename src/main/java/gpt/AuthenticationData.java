package gpt;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationData {
    @JsonProperty("openai")
    HashMap<String, String> openai;
    public String getApiKey() {
        return openai.get( "api-key" );
    }
}
