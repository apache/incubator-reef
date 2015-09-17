/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.reef.bridge.client;

import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.reef.client.DriverRestartConfiguration;
import org.apache.reef.driver.parameters.MaxApplicationSubmissions;
import org.apache.reef.driver.parameters.ResourceManagerPreserveEvaluators;
import org.apache.reef.javabridge.generic.JobDriver;
import org.apache.reef.runtime.common.driver.parameters.ClientRemoteIdentifier;
import org.apache.reef.runtime.common.files.ClasspathProvider;
import org.apache.reef.runtime.common.files.REEFFileNames;
import org.apache.reef.runtime.common.launch.parameters.DriverLaunchCommandPrefix;
import org.apache.reef.runtime.yarn.client.SecurityTokenProvider;
import org.apache.reef.runtime.yarn.client.YarnSubmissionHelper;
import org.apache.reef.runtime.yarn.client.uploader.JobFolder;
import org.apache.reef.runtime.yarn.client.uploader.JobUploader;
import org.apache.reef.runtime.yarn.driver.YarnDriverConfiguration;
import org.apache.reef.runtime.yarn.driver.YarnDriverRestartConfiguration;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Configurations;
import org.apache.reef.tang.Injector;
import org.apache.reef.tang.Tang;
import org.apache.reef.tang.annotations.Name;
import org.apache.reef.tang.annotations.NamedParameter;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.tang.exceptions.InjectionException;
import org.apache.reef.tang.formats.ConfigurationSerializer;
import org.apache.reef.util.JARFileMaker;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Java-side of the C# YARN Job Submission API.
 */
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class YarnJobSubmissionClient {

  private static final Logger LOG = Logger.getLogger(YarnJobSubmissionClient.class.getName());
  private final JobUploader uploader;
  private final ConfigurationSerializer configurationSerializer;
  private final REEFFileNames fileNames;
  private final YarnConfiguration yarnConfiguration;
  private final ClasspathProvider classpath;
  private final int maxApplicationSubmissions;
  private final int driverRestartEvaluatorRecoverySeconds;
  private final SecurityTokenProvider tokenProvider;
  private final List<String> commandPrefixList;

  @Inject
  YarnJobSubmissionClient(final JobUploader uploader,
                          final YarnConfiguration yarnConfiguration,
                          final ConfigurationSerializer configurationSerializer,
                          final REEFFileNames fileNames,
                          final ClasspathProvider classpath,
                          @Parameter(MaxApplicationSubmissions.class)
                          final int maxApplicationSubmissions,
                          @Parameter(DriverLaunchCommandPrefix.class) final List<String> commandPrefixList,
                          @Parameter(SubmissionDriverRestartEvaluatorRecoverySeconds.class)
                          final int driverRestartEvaluatorRecoverySeconds,
                          final SecurityTokenProvider tokenProvider) {
    this.uploader = uploader;
    this.configurationSerializer = configurationSerializer;
    this.fileNames = fileNames;
    this.yarnConfiguration = yarnConfiguration;
    this.classpath = classpath;
    this.maxApplicationSubmissions = maxApplicationSubmissions;
    this.driverRestartEvaluatorRecoverySeconds = driverRestartEvaluatorRecoverySeconds;
    this.tokenProvider = tokenProvider;
    this.commandPrefixList = commandPrefixList;
  }

  private Configuration addYarnDriverConfiguration(final File driverFolder,
                                                   final String jobId,
                                                   final String jobSubmissionFolder)
      throws IOException {
    final File driverConfigurationFile = new File(driverFolder, this.fileNames.getDriverConfigurationPath());
    final Configuration yarnDriverConfiguration = YarnDriverConfiguration.CONF
        .set(YarnDriverConfiguration.JOB_SUBMISSION_DIRECTORY, jobSubmissionFolder)
        .set(YarnDriverConfiguration.JOB_IDENTIFIER, jobId)
        .set(YarnDriverConfiguration.CLIENT_REMOTE_IDENTIFIER, ClientRemoteIdentifier.NONE)
        .set(YarnDriverConfiguration.JVM_HEAP_SLACK, 0.0)
        .build();

    Configuration driverConfiguration = Configurations.merge(
        Constants.DRIVER_CONFIGURATION_WITH_HTTP_AND_NAMESERVER,
        yarnDriverConfiguration);

    if (driverRestartEvaluatorRecoverySeconds > 0) {
      LOG.log(Level.FINE, "Driver restart is enabled.");

      final Configuration yarnDriverRestartConfiguration =
          YarnDriverRestartConfiguration.CONF
              .build();

      final Configuration driverRestartConfiguration =
          DriverRestartConfiguration.CONF
              .set(DriverRestartConfiguration.ON_DRIVER_RESTARTED, JobDriver.RestartHandler.class)
              .set(DriverRestartConfiguration.ON_DRIVER_RESTART_CONTEXT_ACTIVE,
                  JobDriver.DriverRestartActiveContextHandler.class)
              .set(DriverRestartConfiguration.ON_DRIVER_RESTART_TASK_RUNNING,
                  JobDriver.DriverRestartRunningTaskHandler.class)
              .set(DriverRestartConfiguration.DRIVER_RESTART_EVALUATOR_RECOVERY_SECONDS,
                  driverRestartEvaluatorRecoverySeconds)
              .set(DriverRestartConfiguration.ON_DRIVER_RESTART_COMPLETED,
                  JobDriver.DriverRestartCompletedHandler.class)
              .set(DriverRestartConfiguration.ON_DRIVER_RESTART_EVALUATOR_FAILED,
                  JobDriver.DriverRestartFailedEvaluatorHandler.class)
              .build();

      driverConfiguration = Configurations.merge(
          driverConfiguration, yarnDriverRestartConfiguration, driverRestartConfiguration);
    }

    this.configurationSerializer.toFile(driverConfiguration, driverConfigurationFile);
    return driverConfiguration;
  }

  /**
   * @param driverFolder the folder containing the `reef` folder. Only that `reef` folder will be in the JAR.
   * @return
   * @throws IOException
   */
  private File makeJar(final File driverFolder) throws IOException {
    final File jarFile = new File(driverFolder.getParentFile(), driverFolder.getName() + ".jar");
    final File reefFolder = new File(driverFolder, fileNames.getREEFFolderName());
    if (!reefFolder.isDirectory()) {
      throw new FileNotFoundException(reefFolder.getAbsolutePath());
    }

    new JARFileMaker(jarFile).addChildren(reefFolder).close();
    return jarFile;
  }

  private void launch(final YarnSubmissionFromCS yarnSubmission) throws IOException, YarnException {
    // ------------------------------------------------------------------------
    // Get an application ID
    try (final YarnSubmissionHelper submissionHelper =
             new YarnSubmissionHelper(yarnConfiguration, fileNames, classpath, tokenProvider, commandPrefixList)) {


      // ------------------------------------------------------------------------
      // Prepare the JAR
      final JobFolder jobFolderOnDFS = this.uploader.createJobFolder(submissionHelper.getApplicationId());
      final Configuration jobSubmissionConfiguration =
          this.addYarnDriverConfiguration(yarnSubmission.getDriverFolder(), yarnSubmission.getJobId(),
              jobFolderOnDFS.getPath().toString());
      final File jarFile = makeJar(yarnSubmission.getDriverFolder());
      LOG.log(Level.INFO, "Created job submission jar file: {0}", jarFile);


      // ------------------------------------------------------------------------
      // Upload the JAR
      LOG.info("Uploading job submission JAR");
      final LocalResource jarFileOnDFS = jobFolderOnDFS.uploadAsLocalResource(jarFile);
      LOG.info("Uploaded job submission JAR");

      final Injector jobParamsInjector = Tang.Factory.getTang().newInjector(jobSubmissionConfiguration);

      // ------------------------------------------------------------------------
      // Submit
      try {
        submissionHelper
            .addLocalResource(this.fileNames.getREEFFolderName(), jarFileOnDFS)
            .setApplicationName(yarnSubmission.getJobId())
            .setDriverMemory(yarnSubmission.getDriverMemory())
            .setPriority(yarnSubmission.getPriority())
            .setQueue(yarnSubmission.getQueue())
            .setMaxApplicationAttempts(this.maxApplicationSubmissions)
            .setPreserveEvaluators(jobParamsInjector.getNamedInstance(ResourceManagerPreserveEvaluators.class))
            .submit();
      } catch (InjectionException ie) {
        throw new RuntimeException("Unable to submit job due to " + ie);
      }
    }
  }

  /**
   * Takes 5 parameters from the C# side:
   * [0]: String. Driver folder.
   * [1]: String. Driver identifier.
   * [2]: int. Driver memory.
   * [3~5]: int. TCP configurations.
   * [6]: int. Max application submissions.
   * [7]: int. Evaluator recovery timeout for driver restart. > 0 => restart is enabled.
   */
  public static void main(final String[] args) throws InjectionException, IOException, YarnException {
    final YarnSubmissionFromCS yarnSubmission = YarnSubmissionFromCS.fromCommandLine(args);
    LOG.log(Level.INFO, "YARN job submission received from C#: {0}", yarnSubmission);
    final Configuration yarnConfiguration = yarnSubmission.getRuntimeConfiguration();
    final YarnJobSubmissionClient client = Tang.Factory.getTang()
        .newInjector(yarnConfiguration)
        .getInstance(YarnJobSubmissionClient.class);
    client.launch(yarnSubmission);
  }
}

/**
 * How long the driver should wait before timing out on evaluator
 * recovery in seconds. Defaults to -1. If value is negative, the restart functionality will not be
 * enabled. Only used by .NET job submission.
 */
@NamedParameter(doc = "How long the driver should wait before timing out on evaluator" +
    " recovery in seconds. Defaults to -1. If value is negative, the restart functionality will not be" +
    " enabled. Only used by .NET job submission.", default_value = "-1")
final class SubmissionDriverRestartEvaluatorRecoverySeconds implements Name<Integer> {
  private SubmissionDriverRestartEvaluatorRecoverySeconds() {
  }
}
