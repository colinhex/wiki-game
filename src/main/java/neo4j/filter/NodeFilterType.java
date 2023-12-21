package neo4j.filter;

import java.io.Serializable;

public enum NodeFilterType implements Serializable {
    IDENTITY,
    DUPLICATE_FILTER,
    MARK_FILTER
}
