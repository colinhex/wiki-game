import com.theokanning.openai.OpenAiHttpException;
import org.junit.jupiter.api.Test;

public class RateLimitGuardTest {
    private static final String LIMIT = ": Limit ";
    private static final String LIMIT_END = ",";
    private static final String USED = ", Used ";
    private static final String USED_END = ",";
    private static final String REQUESTED = ", Requested ";
    private static final String REQUESTED_END = ".";
    private static final String TRY_AGAIN = "Please try again in ";
    private static final String TRY_AGAIN_END = "ms.";


     @Test
    void testStringStuff() {
         String message = " Rate limit reached for gpt-3.5-turbo-1106 in organization org-AHCn5WSz67dm1tU0zLa08Bcf on tokens_usage_based per min: Limit 60000, Used 59931, Requested 913. Please try again in 844ms.";

         int idxLim = message.indexOf( LIMIT ) + LIMIT.length();
         int idxLimEnd = message.indexOf( LIMIT_END, idxLim );
         int lim = Integer.parseInt( message.substring( idxLim, idxLimEnd ) );

         System.out.println(lim);

         int idxUsed = message.indexOf( USED ) + USED.length();
         int idxUsedEnd = message.indexOf( USED_END, idxUsed );
         int used = Integer.parseInt( message.substring( idxUsed, idxUsedEnd ) );

         System.out.println(used);

         int idxRequested = message.indexOf( REQUESTED ) + REQUESTED.length();
         int idxRequestedEnd = message.indexOf( REQUESTED_END, idxRequested );
         int requested = Integer.parseInt( message.substring( idxRequested, idxRequestedEnd ) );

         System.out.println(requested);

         int idxTryAgain = message.indexOf( TRY_AGAIN ) + TRY_AGAIN.length();
         int idxTryAgainEnd = message.indexOf( TRY_AGAIN_END, idxTryAgain );
         int tryAgain = Integer.parseInt( message.substring( idxTryAgain, idxTryAgainEnd ) );

         System.out.println(tryAgain);

     }

     @Test
    void catchTheThing() {
     }

}
