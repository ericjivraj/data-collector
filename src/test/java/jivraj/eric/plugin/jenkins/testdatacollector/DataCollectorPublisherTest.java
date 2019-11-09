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

public class DataCollectorPublisherTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String databaseUrl = "Database URL";
    final String databaseName = "Database Name";
    final String testReportXMLPath = "XML Path";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath), project.getBuildersList().get(0));
    }

    @Test
    public void testConfigRoundtripFrench() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        DataCollectorPublisher builder = new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath);
        builder.setUseFrench(true);
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);

        DataCollectorPublisher lhs = new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath);
        lhs.setUseFrench(true);
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        DataCollectorPublisher builder = new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Hello, " + databaseUrl, build);
    }

    @Test
    public void testBuildFrench() throws Exception {

        FreeStyleProject project = jenkins.createFreeStyleProject();
        DataCollectorPublisher builder = new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath);
        builder.setUseFrench(true);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Bonjour, " + databaseUrl, build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  greet '" + databaseUrl + "'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Hello, " + databaseUrl + "!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}