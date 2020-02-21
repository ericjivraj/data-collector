package jivraj.eric.plugin.jenkins.testdatacollector;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.UnknownHostException;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class JobResultServiceDAOTest
{
  private MongoClient mongoClientMock;
  private DB databaseMock;
  private static final String database = "test";
  private static final String collection = "results";

  @Test
  public void testCreateNewMongoClientConnectedToLocalhost() throws Exception
  {
    givenMongoClientConnectionIsSetup();
    verifyMongoClientIsSet();
  }

  @Test
  public void testDatabaseIsRetrievedTheMongoClient() throws Exception
  {
    givenMongoClientConnectionIsSetup();
    whenDatabaseIsFetched(database);
    thenAssertDatabaseExists();
  }

  @Test
  public void testCollectionIsRetrievedFromDatabase() throws Exception
  {
    givenMongoClientConnectionIsSetup();
    whenDatabaseIsFetched(database);
    whenCollectionIsFetched(collection);
    thenAssertDatabaseExists();
    thenAssertCollectionExists();
  }

  @Test(expected = Exception.class)
  public void testNotBeAbleToUseMongoClientAfterItHasBeenClosed() throws UnknownHostException
  {
    givenMongoClientConnectionIsSetup();
    whenMongoClientConnectionIsClosed();
    thenAssertMongoClientIsNotReached();
  }

  private void givenMongoClientConnectionIsSetup() throws UnknownHostException
  {
    mongoClientMock = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
  }

  private void whenDatabaseIsFetched(String expectedDatabase)
  {
    databaseMock = mongoClientMock.getDB(expectedDatabase);
  }

  private void whenCollectionIsFetched(String expectedCollection)
  {
    databaseMock.getCollection(expectedCollection);
  }

  private void whenMongoClientConnectionIsClosed()
  {
    mongoClientMock.close();
  }

  private void verifyMongoClientIsSet()
  {
    assertThat(mongoClientMock, is(notNullValue()));
  }

  private void thenAssertDatabaseExists()
  {
    assertThat(database, is(notNullValue()));
  }

  private void thenAssertCollectionExists()
  {
    assertThat(collection, is(notNullValue()));
  }

  private void thenAssertMongoClientIsNotReached()
  {
    mongoClientMock.getDB("SomeDatabase").getCollection("coll").insert(new BasicDBObject("field", "value"));
  }
}
