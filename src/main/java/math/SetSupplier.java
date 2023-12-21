package math;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import mongodb.DocumentGrabber;
import wikigame.WikiGame;

public class SetSupplier {

   public static Set<String> availableNodes( String initialState, String goalState ) {
       Set<String> set = new DocumentGrabber().getDocumentStream()
               .stream()
               .map( WikiGame::getChoices )
               .flatMap( Collection::stream )
               .flatMap( Collection::stream )
               .map( String::valueOf )
               .collect( Collectors.toSet());
       set.add( initialState );
       set.add( goalState );
       return set;
   }

}
