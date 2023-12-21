package neo4j.filter;

import java.io.Serializable;

public enum RelFilterType implements Serializable {
    IDENTITY,
    LIMITED_BFS_25,
    LIMITED_BFS_50,
    LIMITED_BFS_75,
    LIMITED_BFS_100,
    LIMITED_DFS_25,
    LIMITED_DFS_50,
    LIMITED_DFS_75,
    LIMITED_DFS_100,
    MARK_FILTER
}
