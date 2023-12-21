package gpt.prompts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Arguments {
    @JsonProperty("link")
    String link;
    @JsonProperty("reason")
    String reason;

    @Override
    public String toString() {
        return "Link [ "+link+" ] because: " + reason;
    }

    public static Arguments of( String link, String reason ) {
         Arguments arguments = new Arguments();
         arguments.link = link;
         arguments.reason = reason;
         return arguments;
    }

}
