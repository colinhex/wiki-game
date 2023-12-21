package math;

import gpt.CostAccumulator;
import gpt.Model;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import wikigame.WikiGame;


public class WikiGameStatistics {


    private static EvaluationType evaluationTypeFor( WikiGame wikiGame ) {
        return  EvaluationType.of(( wikiGame.getWikiGameConfig().getCot() ? "CoT@":"Shot@") + wikiGame.getWikiGameConfig().getBound() );
    }

    public static Predicate<WikiGameCost> evaluationTypeFilter( EvaluationType evaluationType ) {
        return wikiGameCost -> wikiGameCost.evaluationType == evaluationType;
    }

    public static Predicate<WikiGameCost> identity() {
        return wikiGameCost -> true;
    }


    public static WikiGameCostStatistics of( List<WikiGame> wikiGames ) {
        WikiGameCostStatistics wikiGameCostStatistics = WikiGameCostStatistics.create();
        wikiGames.forEach( wikiGameCostStatistics::add );
        return wikiGameCostStatistics;
    }

    public static WikiGameCostStatistics assumingModel( List<WikiGame> wikiGames, Model model ) {
        WikiGameCostStatistics wikiGameCostStatistics = WikiGameCostStatistics.create();
        wikiGames.forEach( wikiGame -> wikiGameCostStatistics.addAsModel( wikiGame, model ) );
        return wikiGameCostStatistics;
    }


    public static class WikiGameCostDisplay extends HashMap<String, String> {

        private static String formatCurrency( double amount ) {
            return formatCurrency( amount, 1d );
        }

        private static String formatCurrency( double amount, double factor ) {
            return String.format("%.2f $", amount * factor );
        }

        public void render() {
            int maxKeyLength = this.keySet().stream().mapToInt(String::length).max().orElse(0);

            StringBuilder builder = new StringBuilder();
            this.entrySet().stream().sorted( (( o1, o2 ) -> StringUtils.compare( o1.getKey(), o2.getKey() )) ).forEach( entry ->
                    builder.append(String.format("%-" + maxKeyLength + "s : %s%n", entry.getKey(), entry.getValue()))
            );
            System.out.println(builder);
        }

        public static WikiGameCostDisplay with( WikiGameCostStatistics wikiGameCostStatistics ) {
            return with( wikiGameCostStatistics, 1d );
        }

        public static WikiGameCostDisplay with( WikiGameCostStatistics wikiGameCostStatistics, double factor ) {
            WikiGameCostDisplay wikiGameCostDisplay = new WikiGameCostDisplay();
            wikiGameCostDisplay.put( "totalCost", formatCurrency( wikiGameCostStatistics.totalCost(), factor ) );
            wikiGameCostDisplay.put( "averageCost", formatCurrency( wikiGameCostStatistics.averageTotalCost(), factor ) );
            Arrays.stream( EvaluationType.values() ).forEach( evaluationType -> {
                wikiGameCostDisplay.put(
                        "totalCost-" + evaluationType.getName(),
                        formatCurrency( wikiGameCostStatistics.totalCost( evaluationTypeFilter( evaluationType ) ), factor )
                );
                wikiGameCostDisplay.put(
                        "averageCost-" + evaluationType.getName(),
                        formatCurrency( wikiGameCostStatistics.totalCost( evaluationTypeFilter( evaluationType ) ) )
                );
            } );
            wikiGameCostDisplay.put( "tokens", String.valueOf( wikiGameCostStatistics.totalTokens() ) );
            return wikiGameCostDisplay;
        }

    }

    @AllArgsConstructor
    public static class WikiGameCostStatistics {

        @Getter(AccessLevel.PRIVATE)
        private final transient List<WikiGameCost> wikiGameCosts;

        public static WikiGameCostStatistics create() {
            return new WikiGameCostStatistics( new LinkedList<>() );
        }

        public void add( WikiGame wikiGame ) {
           addAsModel( wikiGame, wikiGame.getWikiGameConfig().getModel() );
        }

        public void addAsModel( WikiGame wikiGame, Model model ) {
            getWikiGameCosts().add( new WikiGameCost(
                    wikiGame.getCosts(),
                    model,
                    WikiGameStatistics.evaluationTypeFor( wikiGame )
            ) );
        }

        public double averageTotalCost() {
            return averageTotalCost( identity() );
        }

        public double totalCost() {
            return totalCost( identity() );
        }

        public int totalTokens() {
            return getWikiGameCosts().stream().mapToInt( WikiGameCost::getTotalTokens ).sum();
        }

        public double averageTotalCost( Predicate<WikiGameCost> filter ) {
            return getWikiGameCosts().stream().filter( filter ).mapToDouble( WikiGameCost::totalCost ).average().orElseThrow();
        }

        public double totalCost( Predicate<WikiGameCost> filter ) {
            return getWikiGameCosts().stream().filter( filter ).mapToDouble( WikiGameCost::totalCost ).sum();
        }

    }

    @Getter
    @AllArgsConstructor
    private static class WikiGameCostFactor {
        private double promptTokenCostFactor;
        private double responseTokenCostFactor;

        public static WikiGameCostFactor of( Model model ) {
            return switch ( model ) {
                case GPT_3 -> new WikiGameCostFactor( 0, 0 );
                case GPT_35 -> new WikiGameCostFactor( 0.001/1000, 0.002/1000 );
                case GPT_4 -> new WikiGameCostFactor( 0.03/1000, 0.06/1000 );
            };
        }

    }

    private static class WikiGameCost {

        @Getter
        private final EvaluationType evaluationType;
        @Getter(AccessLevel.PRIVATE)
        private final WikiGameCostFactor wikiGameCostFactor;

        @Getter
        private final int totalTokens;
        @Getter
        private final int promptTokens;
        @Getter
        private final int responseTokens;


        WikiGameCost( CostAccumulator costAccumulator, Model model, EvaluationType evaluationType ) {
            this.evaluationType = evaluationType;
            wikiGameCostFactor = WikiGameCostFactor.of( model );
            totalTokens = costAccumulator.getTotalTokens();
            promptTokens = costAccumulator.getPromptTokens();
            responseTokens = costAccumulator.getResponseTokens();
        }


        public static double totalCost( WikiGameCost wikiGameCost ) {
            return promptCost( wikiGameCost ) + responseCost( wikiGameCost );
        }


        public static double promptCost( WikiGameCost wikiGameCost ) {
            return wikiGameCost.getWikiGameCostFactor().getPromptTokenCostFactor() * wikiGameCost.getPromptTokens();
        }


        public static double responseCost( WikiGameCost wikiGameCost ) {
            return wikiGameCost.getWikiGameCostFactor().getResponseTokenCostFactor() * wikiGameCost.getResponseTokens();
        }

    }


}
