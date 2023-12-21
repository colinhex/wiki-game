package error;

import com.theokanning.openai.Usage;
import gpt.prompts.Arguments;

public class SelfReferenceFunctionCallException extends IllegalFunctionCallException {

    public SelfReferenceFunctionCallException( Arguments call, Usage usage ) {
        super( "You are currently in this state, you can't call it again.", call, usage );
    }

}
