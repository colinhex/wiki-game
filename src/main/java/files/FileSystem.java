package files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileSystem {

    public static String getNeo4jDataDirectory() {
        return "/Users/mandelbrot/.neo4j/wikipedia-082023";
    }

    public static String getNeo4jDatabaseDirectory() {
        return "/Users/mandelbrot/.neo4j/wikipedia-082023-database";
    }


    public static String loadTextFromResourceFile( String file ) {
        try {
            BufferedReader bufferedReader = new BufferedReader( new FileReader(
                    Objects.requireNonNull( FileSystem.class.getClassLoader().getResource( file ) ).getFile()
            ) );
            return bufferedReader.lines().collect( Collectors.joining("\n"));
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }


}
