package gpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import error.AlgorithmException;
import error.BackTrackException;
import error.IllegalFunctionCallException;
import error.InvalidResponseException;
import error.NoFunctionCallException;
import error.NullFunctionCallException;
import error.RateLimitException;
import error.SelfReferenceFunctionCallException;
import gpt.prompts.Arguments;
import gpt.prompts.Context;
import gpt.prompts.Contexts;
import gpt.prompts.WikiGameUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import neo4j.algorithms.State;
import org.apache.commons.lang3.tuple.Pair;
import utils.JsonUtils;
import wikigame.WikiGame;
import wikigame.WikiGames;

@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GptService {

    private static final OpenAiService OPEN_AI_SERVICE;

    private static final RateLimitGuard RATE_LIMIT_GUARD;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        Path path = Path.of( System.getProperty( "user.home" ), ".wgdata", "data", "auth.json" );
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream( path.toFile() );
            String apiKey = objectMapper.readValue( fileInputStream, AuthenticationData.class ).getApiKey();
            OPEN_AI_SERVICE = new OpenAiService( apiKey );
            fileInputStream.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        RATE_LIMIT_GUARD = new RateLimitGuard();
    }

    private final WikiGame wikiGame;

    private GptService( WikiGame wikiGame ) {
        this.wikiGame = wikiGame;
    }

    public static GptService create( WikiGame wikiGame ) {
        return new GptService( wikiGame );
    }

    public Optional<GptServiceResponse> choose( String state, List<String> successors ) throws AlgorithmException {
        Context context = WikiGameUtils.createContext(
                getWikiGame(),
                getWikiGame().getWikiGameConfig().getRandomize() ? Contexts.randomize( successors ) : successors,
                getWikiGame().getPruned(),
                state
        );
        Optional<Pair<Arguments, Usage>> pair = choose( context );
        if (pair.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of( new GptServiceResponse(
                state,
                context.getSuccessors().stream().sorted().toList(),
                pair.get().getLeft(),
                pair.get().getRight()
        ) );
    }

    public Optional<Pair<Arguments, Usage>> choose( Context context ) throws AlgorithmException {
        log.debug("");

        int maxRuns = 5;
        boolean success = false;
        int run = 0;

        while ( !success && run < maxRuns ) {
            log.debug("AT " +  ( getWikiGame().getPath().isEmpty() ? getWikiGame().getState( State.INITIAL ) : getWikiGame().getState( State.GPT_CURRENT ) ) +" CHOOSE ONE OF " + context.getSuccessors() );
            try {
                Pair<Arguments, Usage> choice = chooseAux( context );
                log.debug("CHOICE " + choice.getLeft().getLink() + " BECAUSE " + choice.getLeft().getReason() );
                context.add( choice.getRight() );
                if ( context.getSuccessors().contains( getWikiGame().getState( State.GOAL ) )
                        && ! choice.getLeft().getLink().equals( getWikiGame().getState( State.GOAL ) ) ) {
                    getWikiGame().missGoalState();
                }
                return Optional.of( Pair.of( choice.getLeft(), context.getUsage() ) );
            } catch ( NoFunctionCallException noFunctionCallException ) {
                log.debug( "NO FUNCTION CALL" );
                getWikiGame().hallucinate();
                context.add( noFunctionCallException.getUsage() );
                context.add( Contexts.getSystemMessage( noFunctionCallException.getSystemMessage() ) );
            } catch ( NullFunctionCallException nullFunctionCallException ) {
                log.debug( "NULL FUNCTION CALL" );
                getWikiGame().hallucinate();
                context.add( nullFunctionCallException.getUsage() );
                context.add( Contexts.getSystemMessage( nullFunctionCallException.getSystemMessage() ) );
            } catch ( IllegalFunctionCallException illegalFunctionCallException ) {
                log.debug("ILLEGAL CHOICE: " + illegalFunctionCallException.getArguments().getLink());
                getWikiGame().hallucinate();
                context.add( illegalFunctionCallException.getUsage() );
                context.add( Contexts.getFor( illegalFunctionCallException.getArguments() ) );
                context.add( Contexts.getSystemMessage( illegalFunctionCallException.getSystemMessage() ) );
            } catch ( RateLimitException rateLimitException ) {
                log.debug("RATE LIMIT REACHED");
                try {
                    Thread.sleep( rateLimitException.getWaitTime() );
                    getWikiGame().setHallucinations( getWikiGame().getHallucinations() - 1 );
                    run--;
                } catch ( InterruptedException e ) {
                    log.error( "Thread interrupted", e );
                    throw new RuntimeException( e );
                }
            } catch ( BackTrackException backTrackException ) {
                log.debug("BACKTRACKING TO " + backTrackException.getArguments().getLink());
                try {
                    context.add( backTrackException.getUsage() );
                    Optional<Arguments> backTrack = getWikiGame().getPath().stream().filter( arguments -> arguments.getLink().equals( backTrackException.getArguments().getLink() ) ).findFirst();
                    if (backTrack.isEmpty()) {
                        log.debug("ILLEGAL BACKTRACKING POSITION");
                        getWikiGame().hallucinate();
                        context.add( Contexts.getFor( backTrackException.getArguments() ) );
                        context.add( Contexts.getSystemMessage( backTrackException.getSystemMessage() ) );
                    } else {
                        if ( context.getSuccessors().contains( getWikiGame().getState( State.GOAL ) ) ) {
                            getWikiGame().missGoalState();
                        }
                        WikiGameUtils.backtrack( getWikiGame(), context, backTrack.get() );
                        run--;
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                    throw new AlgorithmException( "Exception occurred during backtracking", e );
                }

            }
            run++;
        }

        return Optional.empty();
    }

    public Pair<Arguments, Usage> chooseAux( Context context ) throws AlgorithmException {
        log.debug("CREATING REQUEST FROM CONTEXT");
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model( context.getModel() )
                .messages( context.getMessages() )
                .functions( context.getFunctions() )
                .functionCall( context.getFunctionCall() )
                .maxTokens( 256 )
                .build();

        ChatCompletionResult chatCompletionResult = request( chatCompletionRequest );

        Usage usage = chatCompletionResult.getUsage();
        ChatMessage responseMessage = chatCompletionResult.getChoices().get( 0 ).getMessage();

        ChatFunctionCall functionCall = responseMessage.getFunctionCall();
        if (functionCall == null) {
            throw new NoFunctionCallException( "Your last response was not a function call. You have to make a function call. ", usage );
        }

        Arguments proposal = JsonUtils.asArguments( functionCall.getArguments() );
        if (proposal.getLink() == null) {
            throw new NullFunctionCallException( "The argument you provide to the parameter 'link' can not be null.", proposal, usage );
        }

        Optional<Arguments> arguments;
        try {
            arguments = context.verifyFunctionCall( proposal );

            if ( arguments.isEmpty() ) {
                arguments = context.isBackTracking( proposal );

                if ( arguments.isEmpty() ) {
                    throw new IllegalFunctionCallException( "You have to provide a provide a valid link. Check that the syntax matches one of the links provided by the user.", proposal, usage );
                }
                if ( getWikiGame().getWikiGameConfig().getBacktracking() ) {
                    throw new BackTrackException( arguments.get(), usage );
                }
            }

        } catch ( SelfReferenceFunctionCallException selfReferenceFunctionCallException ) {
            throw new SelfReferenceFunctionCallException( selfReferenceFunctionCallException.getArguments(), usage );
        }

        return Pair.of( arguments.get(), usage );
    }




    private static synchronized ChatCompletionResult request( ChatCompletionRequest chatCompletionRequest ) throws InvalidResponseException {
        try {
            Thread.sleep( RATE_LIMIT_GUARD.drawTicket() );
            WikiGames.callApi();
            return OPEN_AI_SERVICE.createChatCompletion(chatCompletionRequest);
        } catch ( InterruptedException e ) {
            log.debug("RUN-TIME");
            e.printStackTrace();
            throw new RuntimeException( e );
        } catch ( OpenAiHttpException openAiHttpException ) {
            log.debug("RATE LIMIT");
            openAiHttpException.printStackTrace();
            RATE_LIMIT_GUARD.setRateLimit( openAiHttpException );
            throw new RateLimitException( RATE_LIMIT_GUARD.drawTicket() );
        }
    }


    private static class RateLimitGuard {
        private static final String LIMIT = ": Limit ";
        private static final String LIMIT_END = ",";
        private static final String USED = ", Used ";
        private static final String USED_END = ",";
        private static final String REQUESTED = ", Requested ";
        private static final String REQUESTED_END = ".";
        private static final String TRY_AGAIN = "Please try again in ";
        private static final String TRY_AGAIN_END = "ms.";

        private static final int BUFFER_BIG = 5000;
        private static final int BUFFER_SMALL = 200;

        private boolean receivedLimit = false;
        private long rateLimitReceivedMs = 0;
        private long rateLimitExpires = 0;
        private long ticket;


        public void setRateLimit( OpenAiHttpException openAiHttpException ) {
            log.error( "OpenAiHttpException: ", openAiHttpException );
            String message = openAiHttpException.getMessage();
            if (openAiHttpException.statusCode == 429) {
                rateLimitReceivedMs = System.currentTimeMillis();
                receivedLimit = true;
                int idxLim = message.indexOf( LIMIT ) + LIMIT.length();
                int idxLimEnd = message.indexOf( LIMIT_END, idxLim );
                int lim = Integer.parseInt( message.substring( idxLim, idxLimEnd ) );

                int idxUsed = message.indexOf( USED ) + USED.length();
                int idxUsedEnd = message.indexOf( USED_END, idxUsed );
                int used = Integer.parseInt( message.substring( idxUsed, idxUsedEnd ) );

                int idxRequested = message.indexOf( REQUESTED ) + REQUESTED.length();
                int idxRequestedEnd = message.indexOf( REQUESTED_END, idxRequested );
                int requested = Integer.parseInt( message.substring( idxRequested, idxRequestedEnd ) );

                int idxTryAgain = message.indexOf( TRY_AGAIN ) + TRY_AGAIN.length();
                int idxTryAgainEnd = message.indexOf( TRY_AGAIN_END, idxTryAgain );
                int tryAgain = Integer.parseInt( message.substring( idxTryAgain, idxTryAgainEnd ) );

                rateLimitExpires = rateLimitReceivedMs + tryAgain;
                ticket = 0;
            }

        }


        public long drawTicket() {
            if ( receivedLimit ) {
                long now = System.currentTimeMillis();
                ticket += BUFFER_BIG;
                if (now > rateLimitExpires + ticket) {
                    receivedLimit = false;
                    return BUFFER_SMALL;
                }
                return rateLimitExpires + ticket - rateLimitReceivedMs;
            } else {
                return BUFFER_SMALL;
            }
        }


    }

}
