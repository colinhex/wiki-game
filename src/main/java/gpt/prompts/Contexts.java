package gpt.prompts;

import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatFunctionProperty;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import error.AlgorithmException;
import files.FileSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import neo4j.algorithms.State;
import org.apache.commons.lang3.tuple.Pair;
import utils.JsonUtils;
import wikigame.WikiGame;

public class Contexts {
    private static final String COT = "cot@%s.json";
    private static final String SHOT = "shot@%s.json";


    private static final HashMap<String, String> CONTEXT;

    private static final HashMap<String, Example> EXAMPLES;


    static {
        CONTEXT = new HashMap<>();
        CONTEXT.put( "instruction", FileSystem.loadTextFromResourceFile( "prompt-instruction" ) );
        CONTEXT.put( "intro", FileSystem.loadTextFromResourceFile( "prompt-intro" ) );

        EXAMPLES = new HashMap<>();

        for (int i = 1; i < 6; i++) {
            EXAMPLES.put(
                    String.format( COT, i ),
                    JsonUtils.getExampleAsClass(
                            FileSystem.loadTextFromResourceFile( String.format( COT, i ) )
                    )
            );
            EXAMPLES.put(
                    String.format( SHOT, i ),
                    JsonUtils.getExampleAsClass(
                            FileSystem.loadTextFromResourceFile( String.format( SHOT, i ) )
                    )
            );
        }

    }

    public static String getInstruction() {
        return CONTEXT.get( "instruction" );
    }

    private static String getIntro() {
        return CONTEXT.get( "intro" );
    }


    public static Example getShot( int i ) {
        return EXAMPLES.get( String.format( SHOT, i ) );
    }

    public static Example getCot( int i ) {
        return EXAMPLES.get( String.format( COT, i ) );
    }

    public static List<Example> getShots( int bound ) {
        List<Example> xs = new LinkedList<>();
        for (int i = 1; i <= bound; i++ ) {
            xs.add( getShot( i ) );
        }
        return xs;
    }

    public static List<Example> getCots( int bound ) {
        List<Example> xs = new LinkedList<>();
        for (int i = 1; i <= bound; i++ ) {
            xs.add( getCot( i ) );
        }
        return xs;
    }

    public static List<ChatMessage> merge( List<Example> examples ) {
        List<ChatMessage> messages = new LinkedList<>();
        examples.stream().map( Example::get ).forEach( pair -> {
            messages.add( pair.getLeft() );
            messages.add( pair.getRight() );
        } );
        return messages;
    }

    public static ChatMessage getSystemMessage() {
        return new ChatMessage( ChatMessageRole.SYSTEM.value(), getIntro() );
    }

    public static ChatMessage getInstructionFor( WikiGame wikiGame, List<String> successors ) throws AlgorithmException {
        State state = wikiGame.getPath().isEmpty() ? State.INITIAL : State.GPT_CURRENT;

        boolean backTracking = false;
        List<String> path = null;
        if ( wikiGame.getPath() != null && wikiGame.getPath().size() > 2 && wikiGame.getWikiGameConfig().getBacktracking() ) {
            backTracking = true;
            path = wikiGame.getPath().stream().map( arguments -> arguments.link ).toList();
        }

        return new ChatMessage(
                ChatMessageRole.USER.value(),
                String.format( getInstruction(), wikiGame.getState( state ), wikiGame.getState( State.GOAL ) ) + "\n"
                + "Links:\t" + successors + (backTracking ? "\nYou are also allowed to backtrack to any point in your Path: " + path +
                        ".\n Backtracking to some position in the path will prune all hyperlinks after it, this means you will not be able to choose them again." +
                        ".\n Consider also that you are at depth " + ( wikiGame.getCurrentDepth() + 1 ) + "/" + wikiGame.getWikiGameConfig().getMaxDepth() +
                        ".\n You are not allowed to go over the maximum depth.": "" ) +
                        ( ( backTracking &&  wikiGame.getCurrentDepth() + 1 == wikiGame.getWikiGameConfig().getMaxDepth() ) ? "YOU ARE AT THE MAXIMUM DEPTH. BACKTRACK!": "")
        );

    }


    public static List<ChatMessage> getExamples( int bound, boolean cot ) {
        if ( bound == 0 ) {
            return List.of();
        }

        List<Example> examples = new ArrayList<>( ((cot) ? getCots( bound ) : getShots( bound )) );

        return merge( examples );
    }


    public static Pair<List<ChatMessage>, List<ChatFunctionDynamic>> getFor( WikiGame wikiGame, List<String> successors ) throws AlgorithmException {
        List<ChatMessage> messages = new LinkedList<>();
        messages.add( getSystemMessage() );
        messages.addAll( getExamples( wikiGame.getWikiGameConfig().getBound(), wikiGame.getWikiGameConfig().getCot() ) );
        messages.add( getInstructionFor( wikiGame, successors ) );

        return Pair.of( messages, getChatFunctions() );
    }

    public static ChatFunctionProperty getLinks() {
        return ChatFunctionProperty.builder()
                .name( "link" )
                .description( "Link to click." )
                .type( "string" )
                .required( true )
                .build();
    }

    public static ChatFunctionProperty getReason() {
        return ChatFunctionProperty.builder()
                .name( "reason" )
                .description( "Optional reasoning." )
                .type( "string" )
                .required( true )
                .build();
    }


    public static ChatFunctionDynamic createFunction() {
        return ChatFunctionDynamic.builder()
                .addProperty( getReason() )
                .addProperty( getLinks() )
                .name( "clickLink" )
                .description( "Clicks on a page link." )
                .build();
    }

    public static List<ChatFunctionDynamic> getChatFunctions() {
        ChatFunctionDynamic chatFunctionDynamic = createFunction();
        return List.of(
                chatFunctionDynamic
        );
    }

    public static ChatMessage getFor( Arguments arguments ) {
        ChatMessage chatMessage = new ChatMessage( ChatMessageRole.ASSISTANT.value() );
        ChatFunctionCall chatFunctionCall = new ChatFunctionCall();
        chatFunctionCall.setName( "clickLink" );
        chatFunctionCall.setArguments( JsonUtils.asNode( arguments ) );
        chatMessage.setFunctionCall( chatFunctionCall );
        return chatMessage;
    }

    public static ChatMessage getSystemMessage( String content ) {
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value());
        chatMessage.setContent( content );
        return chatMessage;
    }


    public static <T> ArrayList<T> randomize( List<T> list ) {
        ArrayList<T> xs = new ArrayList<>( list );
        Collections.shuffle( xs );
        return xs;
    }

}
