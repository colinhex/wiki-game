package gpt;

import com.theokanning.openai.Usage;
import gpt.prompts.Arguments;
import java.util.List;
import lombok.Getter;

public record GptServiceResponse( String state, List<String> successors, Arguments arguments, Usage usage ) {}
