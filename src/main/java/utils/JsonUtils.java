package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gpt.prompts.Arguments;
import gpt.prompts.Example;
import gpt.prompts.Contexts;

public class JsonUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }


    public static Example getExampleAsClass( String json ) {
        try {
            return objectMapper.readValue( json, Example.class );
        } catch ( JsonProcessingException e ) {
            throw new RuntimeException( e );
        }
    }

   public static Contexts getInstructionAsClass( String json ) {
       try {
           return objectMapper.readValue( json, Contexts.class );
       } catch ( JsonProcessingException e ) {
           throw new RuntimeException( e );
       }
   }


   public static Arguments getArgumentsAsClass( String json ) {
       try {
           return objectMapper.readValue( json, Arguments.class );
       } catch ( JsonProcessingException e ) {
           throw new RuntimeException( e );
       }
   }

   public static String toJson( Object object, boolean beautify ) {
       try {
           if (beautify) {
               return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString( object );
           }
           return objectMapper.writer().writeValueAsString( object );
       } catch ( JsonProcessingException e ) {
           throw new RuntimeException( e );
       }
   }

   public static JsonNode asNode( Object object ) {
        return objectMapper.valueToTree(object);
   }


   public static Arguments asArguments( JsonNode jsonNode ) {
       try {
           return objectMapper.treeToValue( jsonNode, Arguments.class );
       } catch ( JsonProcessingException e ) {
           throw new RuntimeException( e );
       }
   }
}
