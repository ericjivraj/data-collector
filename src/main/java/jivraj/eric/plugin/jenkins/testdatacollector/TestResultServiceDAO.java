package jivraj.eric.plugin.jenkins.testdatacollector;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class TestResultServiceDAO
{
  private MongoClient mongoClient;
  private DB database;
  private DBCollection collection;

  public TestResultServiceDAO()
  {

  }

  public MongoClient getMongoClient()
  {
    return mongoClient;
  }

  public DB getDatabase()
  {
    return database;
  }

  public DBCollection getCollection()
  {
    return collection;
  }

  public void setMongoClient(MongoClient mongoClient)
  {
    this.mongoClient = mongoClient;
  }

  public void setDatabase(DB database)
  {
    this.database = database;
  }

  public void setCollection(DBCollection collection)
  {
    this.collection = collection;
  }

  public DB fetchMongoDatabase(MongoClient mongoClient, String databaseName)
  {
    return database = mongoClient.getDB(databaseName);
  }

  public DBCollection fetchMongoCollection(String collectionName)
  {
    return database.getCollection(collectionName);
  }

  public MongoClient initiateMongoConnection(MongoClient mongoClient, String URI) throws UnknownHostException
  {
    return mongoClient = new MongoClient(new MongoClientURI(URI));
  }

  public void shutdownMongoConnection(MongoClient mongoClient)
  {
    mongoClient.close();
  }
}
