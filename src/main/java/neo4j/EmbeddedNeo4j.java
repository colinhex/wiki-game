package neo4j;

import files.FileSystem;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import neo4j.logging.Neo4jLogProvider;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;

@Slf4j
public class EmbeddedNeo4j {
    private static final String DEFAULT_DATABASE_NAME= "neo4j";
    @Getter(AccessLevel.MODULE)
    private final DatabaseManagementService databaseManagementService;
    private final GraphDatabaseService graphDatabaseService;
    private static EmbeddedNeo4j instance;

    private EmbeddedNeo4j() {
        log.debug("Initializing Embedded Neo4j Database....");
        databaseManagementService = new DatabaseManagementServiceBuilder( Path.of( FileSystem.getNeo4jDatabaseDirectory() ) )
                .setUserLogProvider( Neo4jLogProvider.getInstance() )
                .setConfig( GraphDatabaseSettings.pagecache_memory, EmbeddedNeo4jConfig.pageCacheMemory )
                .setConfig( GraphDatabaseSettings.preallocate_logical_logs, EmbeddedNeo4jConfig.preallocateLogicalLogs )
                .setConfig( GraphDatabaseSettings.data_directory, Path.of( FileSystem.getNeo4jDataDirectory() ) )
                .build();
        graphDatabaseService = databaseManagementService.database( DEFAULT_DATABASE_NAME );
        registerShutdownHook( databaseManagementService );
        log.debug("Done.");
    }

    public static synchronized EmbeddedNeo4j getInstance() {
        if (instance == null) {
            instance = new EmbeddedNeo4j();
        }
        return instance;
    }

    private void registerShutdownHook( final DatabaseManagementService databaseManagementService ) {
        Runtime.getRuntime().addShutdownHook( new Thread( databaseManagementService::shutdown ) );
    }


    public GraphDatabaseService getService() {
        return this.graphDatabaseService;
    }

}
