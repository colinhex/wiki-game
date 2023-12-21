package math;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EvaluationType {
    SHOT0("Shot@0"),
    SHOT1("Shot@1"),
    SHOT2("Shot@2"),
    SHOT3("Shot@3"),
    SHOT4("Shot@4"),
    SHOT5("Shot@5"),
    COT1("CoT@1"),
    COT2("CoT@2"),
    COT3("CoT@3"),
    COT4("CoT@4"),
    COT5("CoT@5");

    private final String name;

    public static EvaluationType of( String name ) {
        return Arrays.stream( EvaluationType.values() ).filter( evaluationType -> evaluationType.getName().equals( name.trim() ) ).findFirst().orElseThrow();
    }

}
