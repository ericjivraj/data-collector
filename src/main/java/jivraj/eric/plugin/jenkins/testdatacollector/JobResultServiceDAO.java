package jivraj.eric.plugin.jenkins.testdatacollector;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * This class represents the service DAO (Data Access Object) layer
 * It allows the communication between the application and the database
 */
public class JobResultServiceDAO
{
  private MongoClient mongoClient;
  private DB database;
  private DBCollection collection;

  /**
   * Default constructor for this object
   */
  public JobResultServiceDAO()
  {

  }

  /** Getter method for the mongo client connection
   * @return Mongo Client Connection
   */
  public MongoClient getMongoClient()
  {
    return mongoClient;
  }

  /** Setter method for the mongo client connection
   * @param mongoClient Mongo Client Connection
   */
  public void setMongoClient(MongoClient mongoClient)
  {
    this.mongoClient = mongoClient;
  }

  /** Getter method for the mongodb database
   * @return Database
   */
  public DB getDatabase()
  {
    return database;
  }

  /** Setter method for the mongodb database
   * @param database Database
   */
  public void setDatabase(DB database)
  {
    this.database = database;
  }

  /** Getter method for the mongodb collection
   * @return Database Collection
   */
  public DBCollection getCollection()
  {
    return collection;
  }

  /** Setter method for the mongodb collection
   * @param collection Database Collection
   */
  public void setCollection(DBCollection collection)
  {
    this.collection = collection;
  }

  /** Performs a lookup of the mongodb database
   * @param mongoClient Mongo Client Connection (URI/URL)
   * @param databaseName Database Name
   * @return Database object
   */
  public DB fetchMongoDatabase(MongoClient mongoClient, String databaseName)
  {
    return database = mongoClient.getDB(databaseName);
  }

  /** Searches for a given collection in the database
   * @param collectionName Collection Name
   * @return Collection object
   */
  public DBCollection fetchMongoCollection(String collectionName)
  {
    return database.getCollection(collectionName);
  }

  /** Initiates the mongodb connection
   * @param mongoClient Mongo Client Connection
   * @param URI Database URI
   * @return Mongo Client Connection object
   * @throws UnknownHostException is thrown if the host is not recognized or does not exist
   */
  public MongoClient initiateMongoConnection(MongoClient mongoClient, String URI) throws UnknownHostException
  {
    return mongoClient = new MongoClient(new MongoClientURI(URI));
  }

  /** Closes the mongodb connection
   * @param mongoClient Mongo Client Connection
   */
  public void shutdownMongoConnection(MongoClient mongoClient)
  {
    mongoClient.close();
  }
}
