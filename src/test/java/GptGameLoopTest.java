import org.junit.jupiter.api.Test;
import wikigame.WikiGameOrchestrator;
import wikigame.WikiGames;

public class GptGameLoopTest {

    @Test
    void gptGameLoopWorks() {
        WikiGameOrchestrator.setSaveResults( false );
        WikiGames.zeroShot( 1, "Basel", "New_York_Yankees", true, true );
    }

}
