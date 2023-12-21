package gpt.prompts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatMessage;
import error.AlgorithmException;
import error.SelfReferenceFunctionCallException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class Context {

    @JsonIgnoreProperties
    private String model;
    @JsonProperty("messages")
    private List<ChatMessage> messages;
    @JsonIgnoreProperties
    private List<ChatFunctionDynamic> functions;
    private Set<String> successors;
    Set<String> path;
    @JsonIgnoreProperties
    private Usage usage;
    private String state;

    public Context( String model, List<ChatMessage> context, List<ChatFunctionDynamic> functions, List<String> successors, List<String> path, String state ) {
        // System.out.println("------------------------ NEW CONTEXT --------------------------");
        this.model = model;
        this.messages = new ArrayList<>();
        context.forEach( this::add );
        this.functions = functions;
        this.usage = new Usage();
        this.usage.setTotalTokens( 0 );
        this.usage.setCompletionTokens( 0 );
        this.usage.setTotalTokens( 0 );
        this.successors = new HashSet<>( successors );
        this.path = new HashSet<>( path );
        this.state = state;
    }

    public void add( ChatMessage chatMessage ) {
        printChatMessage( chatMessage );
        getMessages().add( chatMessage );
    }

    private void printChatMessage( ChatMessage chatMessage ) {
        if (chatMessage.getFunctionCall() != null) {
            if (chatMessage.getContent() != null) {
                log.debug(chatMessage.getContent());
            }
             log.debug( chatMessage.getRole() + ":\t CHOICE " + chatMessage.getFunctionCall().getArguments().get( "link" ) + " BECAUSE " + chatMessage.getFunctionCall().getArguments().get( "reason" ) );
        } else {
            log.debug( chatMessage.getRole() + ":\t" + chatMessage.getContent() );
        }
    }

    public Optional<Arguments> verifyFunctionCall( Arguments arguments ) throws AlgorithmException {
        Optional<Arguments> args = FunctionCallVerifier.verify( arguments, successors );
        if (args.isPresent()) {
            if (args.get().getLink().equals( getState() )) {
                throw new SelfReferenceFunctionCallException( args.get(), null );
            }
        }
        return args;
    }

    public Optional<Arguments> isBackTracking( Arguments arguments ) throws AlgorithmException {
        Optional<Arguments> args = FunctionCallVerifier.verify( arguments, path );
        if (args.isPresent()) {
            if (args.get().getLink().equals( getState() )) {
                throw new SelfReferenceFunctionCallException( args.get(), null );
            }
        }
        return args;
    }

    public void add( Usage add ) {
        usage.setCompletionTokens( usage.getCompletionTokens() + add.getCompletionTokens() );
        usage.setPromptTokens( usage.getPromptTokens() + add.getPromptTokens() );
        usage.setTotalTokens( usage.getTotalTokens() + add.getTotalTokens() );
    }

    public ChatCompletionRequestFunctionCall getFunctionCall() {
        return ChatCompletionRequestFunctionCall.of( getFunctions().get( 0 ).getName() );
    }


    @Override
    public String toString() {
        return "Context{" +
                "model='" + model + '\'' +
                ", messages=" + messages +
                ", functions=" + functions +
                ", successors=" + successors +
                ", path=" + path +
                ", usage=" + usage +
                '}';
    }

}
