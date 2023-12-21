package wikigame;


import gpt.Model;
import neo4j.filter.RelFilterType;
import neo4j.filter.custom.RestrictedFilter;
import wikigame.WikiGameConfiguration.ConfigurationBuilder;

public class WikiGameConfigurationFactory {

    public static WikiGameConfiguration create( WikiGameType gameType, String initialState, String goalState, Integer examples, Boolean chainOfThought, boolean randomize, boolean backtrack ) {
        ConfigurationBuilder configurationBuilder = WikiGameConfiguration.builder()
                .setGame( gameType )
                .setInitial( initialState )
                .setGoal( goalState );
        switch ( gameType ) {
            case GPT_35_R100_D10 -> {
                return configurationBuilder
                        .setModel( Model.GPT_35 )
                        .setExamples( examples )
                        .setCot( chainOfThought )
                        .setBacktracking( backtrack )
                        .setRandomize( randomize )
                        .setMaxDepth( gameType.getMaxDepth() )
                        .addFilterType( RelFilterType.LIMITED_BFS_100 )
                        .build();
            }
            case SP_R100_D4 -> {
                return configurationBuilder
                        .setMaxDepth( 4 )
                        .build();
            }
            default -> {
                throw new RuntimeException("No such game type.");
            }
        }
    }


}
