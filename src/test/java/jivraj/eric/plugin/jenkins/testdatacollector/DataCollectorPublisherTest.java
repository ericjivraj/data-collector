package jivraj.eric.plugin.jenkins.testdatacollector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class DataCollectorPublisherTest
{
    private final String databaseUrl = "Test Database URL";
    private final String databaseName = "Test Database Name";

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private FreeStyleProject project;
    private FreeStyleBuild build;
    private DataCollectorPublisher publisher;

    @Test
    public void testConfigRoundtrip() throws Exception
    {
        givenFreeStyleProjectIsSetUp();
        givenPublisherIsSetUp();
        whenPublisherIsAddedToPublisherList();
        whenConfigRoundTripIsCalled();
        thenAssertPublisherProperties(publisher.getDatabaseName(), databaseName);
        thenAssertPublisherProperties(publisher.getDatabaseUrl(), databaseUrl);
        thenAssertPublisherHasBeenAdded();
        thenAssertDataBoundBeans();
    }

    @Test
    public void testBuild() throws Exception
    {
        givenFreeStyleProjectIsSetUp();
        givenPublisherIsSetUp();
        whenPublisherIsAddedToPublisherList();
        thenAssertPublisherHasBeenAdded();
        thenAssertPublisherProperties(publisher.getDatabaseName(), databaseName);
        thenAssertPublisherProperties(publisher.getDatabaseUrl(), databaseUrl);
    }

    private void givenFreeStyleProjectIsSetUp() throws IOException
    {
        project = jenkins.createFreeStyleProject();
    }

    private void givenPublisherIsSetUp()
    {
        publisher = new DataCollectorPublisher(databaseUrl, databaseName);
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

    private void thenAssertPublisherHasBeenAdded() throws IOException
    {
        assertNotNull(project.getPublishersList());
    }

    private void thenAssertDataBoundBeans() throws Exception
    {
        jenkins.assertEqualDataBoundBeans(new DataCollectorPublisher(databaseUrl, databaseName), project.getPublishersList().get(0));
    }

    private void thenAssertPublisherProperties(String expected, String actual)
    {
        assertEquals(expected, actual);
    }

}