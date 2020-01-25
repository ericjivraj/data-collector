package jivraj.eric.plugin.jenkins.testdatacollector;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Action;
import hudson.model.Job;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.AggregatedTestResultAction;
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
import java.util.ArrayList;
import java.util.List;

import hudson.util.RunList;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class DataCollectorPublisher extends Recorder implements SimpleBuildStep, Action
{

    private final String databaseUrl;
    private final String databaseName;
    private List<Integer> buildsList = new ArrayList<Integer>();
    private boolean useFrench;

    @DataBoundConstructor
    public DataCollectorPublisher(String databaseUrl, String databaseName)
    {
        this.databaseUrl = databaseUrl;
        this.databaseName = databaseName;
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
        Job project = run.getParent();
        if (!isUpdated(project))
        {
            return;
        }

        buildsList = new ArrayList<>();
        RunList<Run> runs = project.getBuilds();
        for (Run runBuild : runs)
        {
            if (runBuild.isBuilding())
            {
                continue;
            }

            int buildNumber = runBuild.getNumber();
            buildsList.add(buildNumber);

            List<AbstractTestResultAction> testActions = runBuild.getActions(AbstractTestResultAction.class);

            for (AbstractTestResultAction testAction : testActions)
            {
                if (AggregatedTestResultAction.class.isInstance(testAction))
                {
                    addTestResults(buildNumber, (AggregatedTestResultAction) testAction);
                }

                else
                {
                    addTestResult(buildNumber, runBuild, testAction, testAction.getResult());
                }
            }
        }
        listener.getLogger().println("[perform()] Logging:" + databaseUrl + "!");
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

    private void addTestResults(int buildNumber, AggregatedTestResultAction testAction)
    {
        List<AggregatedTestResultAction.ChildReport> childReports = testAction.getChildReports();

        for (AggregatedTestResultAction.ChildReport childReport : childReports)
        {
            addTestResult(buildNumber, childReport.run, testAction, childReport.result);
        }
    }

    private void addTestResult(int buildNumber, Run run, AbstractTestResultAction testAction, Object result)
    {
        if (run == null || result == null)
        {
            return;
        }
    }

    public boolean isUpdated(Job project)
    {
        Run lastBuild = project.getLastBuild();

        if (lastBuild == null)
        {
            return false;
        }

        int latestBuildNumber = lastBuild.getNumber();
        return !(buildsList.contains(latestBuildNumber));
    }

}
