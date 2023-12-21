package error;

import com.theokanning.openai.Usage;
import gpt.prompts.Arguments;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PUBLIC)
public abstract class InvalidResponseException extends GPTException {
    protected String systemMessage;

    protected Arguments arguments;

    protected Usage usage;

    protected InvalidResponseException( String systemMessage, Arguments arguments, Usage usage ) {
        super( "Illegal Response" );
        this.systemMessage = systemMessage;
        this.arguments = arguments;
        this.usage = usage;
    }

}
