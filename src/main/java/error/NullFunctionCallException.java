package error;

import com.theokanning.openai.Usage;
import gpt.prompts.Arguments;
import lombok.Getter;

@Getter
public class NullFunctionCallException extends IllegalFunctionCallException {
    public NullFunctionCallException( String systemMessage, Arguments arguments, Usage usage ) {
        super( systemMessage, arguments, usage );
    }

}
