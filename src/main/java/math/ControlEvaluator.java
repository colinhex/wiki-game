package math;

import java.util.List;
import wikigame.WikiGame;

public class ControlEvaluator {

    public static void evaluate( WikiGame wikiGame ) {
        List<List<String>> paths = wikiGame.getTraversalResult();
        long pc1 = paths.stream().filter( path -> path.size() == 1 ).count();
        long pc2 = paths.stream().filter( path -> path.size() == 2 ).count();
        long pc3 = paths.stream().filter( path -> path.size() == 3 ).count();
        long pc4 = paths.stream().filter( path -> path.size() == 4 ).count();
        long pc5 = paths.stream().filter( path -> path.size() == 5 ).count();
        // long pc6 = paths.stream().filter( path -> path.size() == 6 ).count();

        System.out.printf( "pc0=%s%npc1=%s%npc2=%s%npc3=%s"/*"npc6=%s%n"*/, pc1, pc2, pc3, pc4 );



    }

}
