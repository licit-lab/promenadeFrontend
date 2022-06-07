package data.mongo;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


//TODO: handle replica set
public class MongoConnectionManager {

//    public static final String DEFAULT_MONGO_HOSTNAME = "localhost";
//    public static final int DEFAULT_MONGO_PORT = 27017;
//    public static final String DATABASE_NAME = "promenade";
//    public static final String DEFAULT_COLLECTION_NAME = "areas";
//
    private final MongoClient mongo;
    private final String hostname, port, database;

//    public MongoConnectionManager(String hostname, int port) {
//        String connectionString = "mongodb://" + hostname + ":" + port;
//        this.mongo = MongoClients.create(connectionString);
//    }
//
//    public MongoConnectionManager(String hostname) {
//        this(hostname, DEFAULT_MONGO_PORT);
//    }
//
//    public MongoConnectionManager() {
//        String hostname = System.getenv("MONGO_HOSTNAME");
//        if (hostname == null){
//            hostname = DEFAULT_MONGO_HOSTNAME;
//        }
//        String connectionString = "mongodb://" + hostname + ":" + DEFAULT_MONGO_PORT;
//        this.mongo = MongoClients.create(connectionString);
//    }


    public MongoConnectionManager(String hostname, String port, String database) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        String connectionString = "mongodb://" + hostname + ":" + port;
        this.mongo =  MongoClients.create(connectionString);
    }

    public MongoClient getClient() {
        return mongo;
    }

    public void close() {
        mongo.close();
    }
    
    public MongoDatabase getDatabase() {
    	CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
    	                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    	return this.mongo.getDatabase(this.database).withCodecRegistry(pojoCodecRegistry);
    }
    
    public <T> MongoCollection<T> getCollection(String collection, Class<T> documentClass) {
//    	if(collection == null)
//    		return this.getDatabase().getCollection(conf.getProperty("mongo.collection.name"), documentClass);
//    	else
    		return this.getDatabase().getCollection(collection, documentClass);
    }
}