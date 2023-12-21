package gpt.prompts;

import error.AlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import utils.MathUtils;

@Slf4j
public class FunctionCallVerifier {
    private static final String MATCH_FAILURE = "MATCHED NOTHING WITH %s";
    private static final String MATCH_LOG_TEMPLATE = "MATCHED %s WITH %s ";
    private static final String EXACTLY = "EXACTLY";
    private static final String LONELY_SUBSTRING = "WITH LONELY SUBSTRING";
    private static final String UNDERSCORES_REPLACED = "WITH UNDERSCORES REPLACED";
    private static final String LEVENSHTEIN = "WITH LEVENSHTEIN SCORE %s";
    private static final int LEVENSHTEIN_THRESHOLD = 3;
    private final Arguments arguments;
    private final Set<String> validOptions;
    private Integer score;
    private String match;

    private FunctionCallVerifier( Arguments arguments, Collection<String> validOptions ) {
        this.arguments = arguments;
        this.validOptions = new HashSet<>( validOptions );
        this.match = null;
    }

    public static Optional<Arguments> verify( Arguments arguments, Collection<String> validOptions ) {
        return new FunctionCallVerifier( arguments, validOptions ).getValid();
    }

    private Optional<Arguments> getValid() {
        matchesExactly();

        if ( matches() ) {
            log.debug( String.format( MATCH_LOG_TEMPLATE + EXACTLY + "%n", arguments.getLink(), match ) );
            return getMatch();
        }

        isSubstringOfExactlyOne();

        if ( matches() ) {
            log.debug( String.format( MATCH_LOG_TEMPLATE + LONELY_SUBSTRING + "%n", arguments.getLink(), match ) );
            return getMatch();
        }

        matchesWithSpaces();

        if ( matches() ) {
            log.debug( String.format( MATCH_LOG_TEMPLATE + UNDERSCORES_REPLACED + "%n", arguments.getLink(), match ) );
            return getMatch();
        }

        matchesWithStringDistance();

        if ( matches() ) {
            log.debug( String.format( MATCH_LOG_TEMPLATE + LEVENSHTEIN + "%n", arguments.getLink(), match, score ) );
            return getMatch();
        }

        log.debug( String.format( MATCH_FAILURE + "%n", arguments.getLink()) );
        return Optional.empty();
    }



    private boolean matches() {
        return match != null;
    }

    private Optional<Arguments> getMatch() {
        return Optional.of( Arguments.of( match, arguments.getReason() ) );
    }


    private void matchesExactly() {
        if ( this.validOptions.contains( arguments.getLink() ) ) {
            match = arguments.getLink();
        };
    }

    private void matchesWithSpaces() {
        this.validOptions
                .stream()
                .map( link -> link.replace( "_", " " ) )
                .filter( link -> link.equals( arguments.getLink() ) )
                .map( link -> link.replace( " ", "_" ) )
                .findFirst()
                .ifPresent( link -> {
                    match = link;
                } );
    }

    private void isSubstringOfExactlyOne() {
        List<String> xs = this.validOptions
                .stream()
                .filter( link -> link.contains( arguments.getLink() ) )
                .toList();
        if ( xs.size() == 1 ) {
            match = xs.get( 0 );
        }
    }


    private void matchesWithStringDistance() {
        computeStringDistanceVector()
                .filter( pair -> pair.getLeft() <= LEVENSHTEIN_THRESHOLD )
                .min( Comparator.comparingInt( Pair::getLeft ) )
                .ifPresent( pair -> {
                    match = pair.getRight();
                    score = pair.getLeft();
                } );
    }

    private Stream<Pair<Integer, String>> computeStringDistanceVector() {
        return validOptions.stream()
                .map( actual -> Pair.of( computeStringDistance( arguments.getLink(), actual ), actual ) );
    }

    private int computeStringDistance( String received, String actual ) {
        return MathUtils.levenshtein( received, actual );
    }


}
