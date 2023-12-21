package error;

import com.theokanning.openai.Usage;
import gpt.prompts.Arguments;

public class IllegalFunctionCallException extends InvalidResponseException {

    public IllegalFunctionCallException( String systemMessage, Arguments arguments, Usage usage ) {
        super( systemMessage, arguments, usage );
    }

}
