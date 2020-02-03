package jivraj.eric.plugin.jenkins.testdatacollector;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Job;
import jenkins.model.TransientActionFactory;

@Extension
public class DataCollectorExtension extends TransientActionFactory<Job> implements Describable<DataCollectorExtension>
{
  private String databaseUrl;
  private String databaseName;

  @Extension
  public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

  @Override
  public Descriptor<DataCollectorExtension> getDescriptor()
  {
    return DESCRIPTOR;
  }

  @Override
  public Class<Job> type()
  {
    return Job.class;
  }

  @Nonnull
  @Override
  public Collection<? extends Action> createFor(@Nonnull Job job)
  {
    return Collections.singleton(new DataCollectorPublisher(databaseUrl, databaseName));
  }

  public static class DescriptorImpl extends Descriptor<DataCollectorExtension>
  {
    public DescriptorImpl() {
      load();
    }
  }
}
