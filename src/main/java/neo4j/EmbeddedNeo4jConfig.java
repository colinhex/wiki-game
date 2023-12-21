package neo4j;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Duration;
import org.neo4j.io.ByteUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddedNeo4jConfig implements Serializable {
    @JsonProperty("nodeType")
    public static final String nodeType = "Node";

    @JsonProperty("titleProperty")
    public static final String titleProperty = "title";
    @JsonProperty("pageCacheMemory")
    public static final Long pageCacheMemory = ByteUnit.mebiBytes( 4096 );

    @JsonProperty("transactionTimeout")
    public static final Duration transactionTimeout = Duration.ofSeconds( 60 );

    @JsonProperty("preallocateLogicalLogs")
    public static final Boolean preallocateLogicalLogs = false;

    @JsonProperty("readOnly")
    public static final Boolean readOnly = true;

}
