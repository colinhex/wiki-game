package error;

import com.theokanning.openai.Usage;
import gpt.prompts.Arguments;

public class BackTrackException extends InvalidResponseException {
    public BackTrackException( Arguments arguments, Usage usage ) {
        super("You can only backtrack to a state you have previously been to.", arguments, usage);
    }

}
