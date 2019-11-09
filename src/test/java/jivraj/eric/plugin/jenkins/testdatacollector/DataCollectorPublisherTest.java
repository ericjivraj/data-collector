package jivraj.eric.plugin.jenkins.testdatacollector;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class DataCollectorPublisherTest
{

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String databaseUrl = "Database URL";
    final String databaseName = "Database Name";
    final String testReportXMLPath = "XML Path";

    @Test
    public void testConfigRoundtrip() throws Exception
    {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getPublishersList().add(new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath), project.getPublishersList().get(0));
    }

    @Test
    public void testBuild() throws Exception
    {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        DataCollectorPublisher publisher = new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath);
        project.getPublishersList().add(publisher);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Hello, " + databaseUrl, build);
    }

}