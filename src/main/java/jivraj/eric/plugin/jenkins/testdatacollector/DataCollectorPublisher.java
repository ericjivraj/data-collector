package jivraj.eric.plugin.jenkins.testdatacollector;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Action;
import hudson.model.Job;
import hudson.plugins.git.Branch;
import hudson.plugins.git.util.BuildData;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.Symbol;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/** This can be seen as the Application class
 * This is the Data Collector Publisher class, and this object is responsible for handling the build plugin running and executing behaviour
 * Extending the Recorder interface makes this plugin a post-build plugin
 * This means that this plugin only runs after the Jenkins build has completed
 */
public class DataCollectorPublisher extends Recorder implements SimpleBuildStep, Action
{
  private JobResultServiceDAO mongoService;
  private MongoClient mongoClient;
  private DB database;
  private DBCollection collection;
  private final String databaseUrl;
  private final String databaseName;

  /** Default constructor for the object
   * @param databaseUrl Database URL
   * @param databaseName Database Name
   */
  @DataBoundConstructor
  public DataCollectorPublisher(String databaseUrl, String databaseName)
  {
    this.databaseUrl = databaseUrl;
    this.databaseName = databaseName;
    mongoService = new JobResultServiceDAO();
  }

  /** Getter method for the database url
   * @return Database URL
   */
  public String getDatabaseUrl()
  {
    return databaseUrl;
  }

  /** Getter method for the database name
   * @return Database Name
   */
  public String getDatabaseName()
  {
    return databaseName;
  }

  /** This is the method that gets executed and called when a Jenkins build finishes
   * As soon as the Jenkins build finishes, this method gets triggered so the plugin starts running
   * @param run Run object that represents the build run
   * @param workspace Workspace object that represents the jenkins workspace
   * @param launcher Launcher object that represents the launcher
   * @param listener Listener object that can be used for logging purposes
   * @throws InterruptedException
   * @throws IOException
   */
  @Override
  public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException
  {
    listener.getLogger().println("[Data Collector Plugin] Started running...");

    initiateMongoConnection();
    fetchMongoDatabase();
    fetchMongoCollection("JobResults");

    Object result;
    AbstractTestResultAction testResultAction = run.getAction(AbstractTestResultAction.class);
    result = testResultAction.getResult();

    if (result instanceof TestResult)
    {
      BuildData buildData = run.getAction(BuildData.class);
      Collection<Branch> branches = buildData.getLastBuiltRevision().getBranches();
      String branchName = branches.iterator().next().getName();
      String buildRevision = String.valueOf(buildData.getLastBuiltRevision());

      TestResult testResult = (TestResult) result;
      String testJob = testResult.getRun().getParent().getDisplayName();
      String buildNumber = String.valueOf(testResult.getRun().getNumber());
      String buildStatus;
      if(testResult.getBuildResult() == null)
      {
        buildStatus = "PASSED";
      }
      else
        {
          buildStatus = String.valueOf(testResult.getBuildResult());
        }

      Map<String, List<DBObject>> testResultsMap = new HashMap<>();
      for(TestResult passedTest: testResult.getPassedTests())
      {
        String className = ((CaseResult) passedTest).getClassName();
        String testName = passedTest.getName();
        String testStatus = String.valueOf(((CaseResult) passedTest).getStatus());

        DBObject testResultObject = new BasicDBObject("className", className)
                .append("testName", testName)
                .append("testStatus", testStatus);

        List<DBObject> testResultsList = testResultsMap.get(className);
        if(testResultsList == null)
        {
          testResultsList = new ArrayList<>();
          testResultsMap.put(className, testResultsList);
        }

        testResultsList.add(testResultObject);
      }

      for(TestResult failedTest : testResult.getFailedTests())
      {
        String className = ((CaseResult) failedTest).getClassName();
        String testName = failedTest.getName();
        String testStatus = String.valueOf(((CaseResult) failedTest).getStatus());
        String stackTrace = failedTest.getErrorStackTrace();

        DBObject testResultObject = new BasicDBObject("className", className)
                .append("testName", testName)
                .append("testStatus", testStatus)
                .append("stackTrace", stackTrace);

        List<DBObject> testResultsList = testResultsMap.get(className);
        if(testResultsList == null)
        {
          testResultsList = new ArrayList<>();
          testResultsMap.put(className, testResultsList);
        }

        testResultsList.add(testResultObject);
      }

      DBObject jobResult = new BasicDBObject("testJob", testJob)
              .append("buildNo", buildNumber)
              .append("buildStatus", buildStatus)
              .append("buildRevision", buildRevision)
              .append("branch", branchName)
              .append("testResults", testResultsMap);

      collection.insert(jobResult);

      listener.getLogger().println("[Data Collector Plugin] Success: Data has been collected and stored into the database...");
    }

    else
    {
      listener.getLogger().println("[Data Collector Plugin] Error: The result is not of the type expected! The type expected was Test Result...");
    }

    shutdownMongoConnection(mongoClient);
    listener.getLogger().println("[Data Collector Plugin] Finished running...");
  }

  /** Initiates mongodb connection using the DAO layer object
   * @throws UnknownHostException if host is not recognized or does not exist
   */
  private void initiateMongoConnection() throws UnknownHostException
  {
    mongoClient = mongoService.initiateMongoConnection(mongoClient, databaseUrl);
  }

  /**
   * Fetches the mongo database using the DAO layer object
   */
  private void fetchMongoDatabase()
  {
    database = mongoService.fetchMongoDatabase(mongoClient, databaseName);
  }

  /** Fetches the mongo collection using the DAO layer object
   * @param collectionName Collection Name
   */
  private void fetchMongoCollection(String collectionName)
  {
    collection = mongoService.fetchMongoCollection(collectionName);
  }

  /** Shuts down the mongo connection
   * @param mongoClient Mongo Client Connection
   */
  private void shutdownMongoConnection(MongoClient mongoClient)
  {
    mongoService.shutdownMongoConnection(mongoClient);
  }

  @Override
  public BuildStepMonitor getRequiredMonitorService()
  {
    return BuildStepMonitor.NONE;
  }

  @CheckForNull
  @Override
  public String getIconFileName()
  {
    return null;
  }

  @CheckForNull
  @Override
  public String getDisplayName()
  {
    return null;
  }

  @CheckForNull
  @Override
  public String getUrlName()
  {
    return null;
  }

  @Symbol("greet")
  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Publisher>
  {

    public FormValidation doCheckDatabaseUrl(@QueryParameter String databaseUrl) throws IOException, ServletException
    {
      if (databaseUrl.length() == 0)
      {
        return FormValidation.error(Messages.DataCollectorPublisher_DescriptorImpl_errors_missingDatabaseUrl());
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckDatabaseName(@QueryParameter String databaseName) throws IOException, ServletException
    {
      if (databaseName.length() == 0)
      {
        return FormValidation.error(Messages.DataCollectorPublisher_DescriptorImpl_errors_missingDatabaseName());
      }

      return FormValidation.ok();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass)
    {
      return true;
    }

    @Override
    public String getDisplayName()
    {
      return Messages.DataCollectorPublisher_DescriptorImpl_DisplayCollectDataPostBuildAction();
    }
  }
}
