package mongodb;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import wikigame.WikiGame;

@Slf4j
@Getter
public class DocumentSaver {
    private static final String DATABASE_NAME = "wiki";
    private static final String COLLECTION_NAME = "gpt_simple_2";

    private static final String CRED_DATABASE_NAME = "admin";

    private static final String CRED_USER = "root";

    private static final String CRED_PASSWORD = "root";

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> mongoCollection;
    private final ObjectMapper objectMapper;

    public DocumentSaver() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applicationName( "WikiGames" )
                .credential( MongoCredential.createCredential( CRED_USER, CRED_DATABASE_NAME, CRED_PASSWORD.toCharArray() ) )
                .applyConnectionString( new ConnectionString( "mongodb://0.0.0.0:27017"))
                .build();
        mongoClient = MongoClients.create(settings);
        mongoDatabase = getMongoClient().getDatabase(DATABASE_NAME);
        mongoCollection = mongoDatabase.getCollection(COLLECTION_NAME);
        objectMapper = new ObjectMapper();
    }

    public String mapToJson( WikiGame wikiGame ) {
        String json;
        try {
            json = getObjectMapper().writeValueAsString( wikiGame );
        } catch ( JsonProcessingException e ) {
            throw new RuntimeException( e );
        }
        if ( log.isTraceEnabled() ) {
            log.trace( "Json Rep: " + json );
        }
        return json;
    }

    public Document mapToDocument( String json ) {
        return Document.parse( json );
    }

    public void insert( Document document ) {
        getMongoCollection().insertOne( document );
    }

    public void close() {
        if (getMongoClient() != null) {
            getMongoClient().close();
        }
    }

}
