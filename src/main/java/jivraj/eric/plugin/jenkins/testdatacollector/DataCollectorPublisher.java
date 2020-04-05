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
import org.kohsuke.stapler.DataBoundSetter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class DataCollectorPublisher extends Recorder implements SimpleBuildStep, Action
{

  private JobResultServiceDAO mongoService;
  private MongoClient mongoClient;
  private DB database;
  private DBCollection collection;
  private Job project;
  private final String databaseUrl;
  private final String databaseName;
  private List<Integer> buildsList = new ArrayList<>();
  private boolean useFrench;

  @DataBoundConstructor
  public DataCollectorPublisher(String databaseUrl, String databaseName)
  {
    this.databaseUrl = databaseUrl;
    this.databaseName = databaseName;
    mongoService = new JobResultServiceDAO();
  }

  public String getDatabaseUrl()
  {
    return databaseUrl;
  }

  public String getDatabaseName()
  {
    return databaseName;
  }

  public boolean isUseFrench()
  {
    return useFrench;
  }

  @DataBoundSetter
  public void setUseFrench(boolean useFrench)
  {
    this.useFrench = useFrench;
  }

  @Override
  public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException
  {
    listener.getLogger().println("[Data Collector Plugin] Started running...");

    initiateMongoConnection();
    fetchMongoDatabase();
    fetchMongoCollection("JobResults");

    project = run.getParent();
    if (!isUpdated(project))
    {
      return;
    }

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

      DBObject testSuite = new BasicDBObject("testJob", testJob)
              .append("buildNo", buildNumber)
              .append("buildStatus", buildStatus)
              .append("buildRevision", buildRevision)
              .append("branch", branchName)
              .append("testResults", testResultsMap);

      collection.insert(testSuite);

      listener.getLogger().println("[Data Collector Plugin] Success: Data has been collected and stored into the database...");
    }

    else
    {
      listener.getLogger().println("[Data Collector Plugin] Error: The result is not of the type expected! The type expected was Test Result...");
    }

    shutdownMongoConnection(mongoClient);
    listener.getLogger().println("[Data Collector Plugin] Finished running...");
  }

  private void initiateMongoConnection() throws UnknownHostException
  {
    mongoClient = mongoService.initiateMongoConnection(mongoClient, databaseUrl);
  }

  private void fetchMongoDatabase()
  {
    database = mongoService.fetchMongoDatabase(mongoClient, databaseName);
  }

  private void fetchMongoCollection(String collectionName)
  {
    collection = mongoService.fetchMongoCollection(collectionName);
  }

  private void shutdownMongoConnection(MongoClient mongoClient)
  {
    mongoService.shutdownMongoConnection(mongoClient);
  }

  private boolean isUpdated(Job project)
  {
    Run lastBuild = project.getLastBuild();

    if (lastBuild == null)
    {
      return false;
    }

    int latestBuildNumber = lastBuild.getNumber();
    return !(buildsList.contains(latestBuildNumber));
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
