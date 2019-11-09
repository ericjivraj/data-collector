package jivraj.eric.plugin.jenkins.testdatacollector;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class DataCollectorPublisher extends Recorder implements SimpleBuildStep
{

    private final String databaseUrl;
    private final String databaseName;
    private final String testReportXMLPath;
    private boolean useFrench;

    @DataBoundConstructor
    public DataCollectorPublisher(String databaseUrl, String databaseName, String testReportXMLPath)
    {
        this.databaseUrl = databaseUrl;
        this.databaseName = databaseName;
        this.testReportXMLPath = testReportXMLPath;
    }

    public String getDatabaseUrl()
    {
        return databaseUrl;
    }

    public String getDatabaseName()
    {
        return databaseName;
    }

    public String getTestReportXMLPath()
    {
        return testReportXMLPath;
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
        // To add an action object to the run object, simply do run.addAction(object) in this method
        if (useFrench) {
            listener.getLogger().println("Bonjour, " + databaseUrl + "!");
        } else {
            listener.getLogger().println("Hello, " + databaseUrl + "!");
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.NONE;
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

        public FormValidation doCheckTestReportXMLPath(@QueryParameter String testReportXMLPath) throws IOException, ServletException
        {
            if (testReportXMLPath.length() == 0)
            {
                return FormValidation.error(Messages.DataCollectorPublisher_DescriptorImpl_errors_missingTestReportXMLPath());
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
