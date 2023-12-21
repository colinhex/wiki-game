package error;

import org.neo4j.graphdb.Node;

public class SuccessorsNotFoundException extends AlgorithmException {


    public SuccessorsNotFoundException( String message ) {
        super( message );
    }

    public static SuccessorsNotFoundException create( String state ) {
        return new SuccessorsNotFoundException( String.format( "No successors returned for state %s", state ) );
    }

}
