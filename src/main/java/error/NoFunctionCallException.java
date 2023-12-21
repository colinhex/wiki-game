package error;

import com.theokanning.openai.Usage;

public class NoFunctionCallException extends InvalidResponseException {
    public NoFunctionCallException( String systemMessage, Usage usage ) {
        super(systemMessage, null, usage);
    }

}
