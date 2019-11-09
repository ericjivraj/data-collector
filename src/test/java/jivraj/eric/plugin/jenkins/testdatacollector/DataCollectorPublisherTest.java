package jivraj.eric.plugin.jenkins.testdatacollector;

import java.io.IOException;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class DataCollectorPublisherTest
{
    public final String databaseUrl = "Database URL";
    public final String databaseName = "Database Name";
    public final String testReportXMLPath = "XML Path";

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    public FreeStyleProject project;
    public FreeStyleBuild build;
    public DataCollectorPublisher publisher;


    @Test
    public void testConfigRoundtrip() throws Exception
    {
        givenFreeStyleProjectIsSetUp();
        givenPublisherIsSetUp();
        whenPublisherIsAddedToPublisherList();
        whenConfigRoundTripIsCalled();
        thenAssertDataBoundBeans();
    }

    @Test
    public void testBuild() throws Exception
    {
        givenFreeStyleProjectIsSetUp();
        givenPublisherIsSetUp();
        whenPublisherIsAddedToPublisherList();
        thenAssertBuildStatusSuccess();
        thenAssertLogContainsCorrectValues();
    }

    private void givenFreeStyleProjectIsSetUp() throws IOException
    {
        project = jenkins.createFreeStyleProject();
    }

    private void givenPublisherIsSetUp()
    {
        publisher = new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath);
    }

    private void whenPublisherIsAddedToPublisherList()
    {
        project.getPublishersList().add(publisher);
    }

    private void whenConfigRoundTripIsCalled() throws Exception
    {
        project = jenkins.configRoundtrip(project);
    }

    private void thenAssertBuildStatusSuccess() throws Exception
    {
        build = jenkins.buildAndAssertSuccess(project);
    }

    private void thenAssertLogContainsCorrectValues() throws IOException
    {
        jenkins.assertLogContains("Hello, " + databaseUrl, build);
    }

    private void thenAssertDataBoundBeans() throws Exception
    {
        jenkins.assertEqualDataBoundBeans(new DataCollectorPublisher(databaseUrl, databaseName, testReportXMLPath), project.getPublishersList().get(0));
    }

}