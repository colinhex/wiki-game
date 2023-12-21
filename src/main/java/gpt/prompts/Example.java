package gpt.prompts;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import utils.JsonUtils;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Example {

    @JsonProperty("current_page")
    String currentPage;

    @JsonProperty("goal_page")
    String goalPage;

    @JsonProperty("links")
    List<String> links;

    @JsonProperty("arguments")
    Arguments arguments;

    private ChatMessage getExampleInstructionMessage() {
        return new ChatMessage(
                ChatMessageRole.USER.value(),
                String.format( Contexts.getInstruction(), getCurrentPage(), getGoalPage() ) + "\n" +
                String.format( "Links: " + getLinks() )
        );
    }

    private ChatMessage getExampleFunctionCallMessage() {
        ChatMessage chatMessage = new ChatMessage( ChatMessageRole.ASSISTANT.value() );
        chatMessage.setFunctionCall( new ChatFunctionCall(
                "clickLink", JsonUtils.asNode( getArguments() )
        ) );
        return chatMessage;
    }

    public Pair<ChatMessage, ChatMessage> get() {
        return Pair.of( getExampleInstructionMessage(), getExampleFunctionCallMessage() );
    }



}
