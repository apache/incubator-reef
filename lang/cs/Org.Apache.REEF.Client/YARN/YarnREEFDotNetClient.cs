﻿// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
// 
//   http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Org.Apache.REEF.Client.API;
using Org.Apache.REEF.Client.Avro;
using Org.Apache.REEF.Client.Avro.YARN;
using Org.Apache.REEF.Client.Common;
using Org.Apache.REEF.Client.Yarn;
using Org.Apache.REEF.Client.Yarn.RestClient;
using Org.Apache.REEF.Client.YARN.RestClient.DataModel;
using Org.Apache.REEF.Common.Avro;
using Org.Apache.REEF.Common.Files;
using Org.Apache.REEF.Driver.Bridge;
using Org.Apache.REEF.Tang.Annotations;
using Org.Apache.REEF.Tang.Implementations.Tang;
using Org.Apache.REEF.Tang.Interface;
using Org.Apache.REEF.Utilities.Attributes;
using Org.Apache.REEF.Utilities.Logging;
using Org.Apache.REEF.Wake.Remote.Parameters;

namespace Org.Apache.REEF.Client.YARN
{
    /// <summary>
    /// Temporary client for developing and testing .NET job submission E2E.
    /// TODO: When REEF-189 is completed YARNREEFClient should be either merged or
    /// deprecated by final client.
    /// </summary>
    [Unstable("For security token support we still need to use YARNREEFClient until (REEF-875)")]
    public sealed class YarnREEFDotNetClient : IREEFClient
    {
        private const string REEFApplicationType = @"REEF";
        private static readonly Logger Log = Logger.GetLogger(typeof(YarnREEFDotNetClient));
        private readonly IYarnRMClient _yarnRMClient;
        private readonly DriverFolderPreparationHelper _driverFolderPreparationHelper;
        private readonly IJobResourceUploader _jobResourceUploader;
        private readonly IYarnJobCommandProvider _yarnJobCommandProvider;
        private readonly REEFFileNames _fileNames;
        private readonly IJobSubmissionDirectoryProvider _jobSubmissionDirectoryProvider;

        [Inject]
        private YarnREEFDotNetClient(
            IYarnRMClient yarnRMClient,
            DriverFolderPreparationHelper driverFolderPreparationHelper,
            IJobResourceUploader jobResourceUploader,
            IYarnJobCommandProvider yarnJobCommandProvider,
            REEFFileNames fileNames,
            IJobSubmissionDirectoryProvider jobSubmissionDirectoryProvider)
        {
            _jobSubmissionDirectoryProvider = jobSubmissionDirectoryProvider;
            _fileNames = fileNames;
            _yarnJobCommandProvider = yarnJobCommandProvider;
            _jobResourceUploader = jobResourceUploader;
            _driverFolderPreparationHelper = driverFolderPreparationHelper;
            _yarnRMClient = yarnRMClient;
        }

        public void Submit(IJobSubmission jobSubmission)
        {
            string jobId = jobSubmission.JobIdentifier;

            // todo: Future client interface should be async.
            // Using GetAwaiter().GetResult() instead of .Result to avoid exception
            // getting wrapped in AggregateException.
            var newApplication = _yarnRMClient.CreateNewApplicationAsync().GetAwaiter().GetResult();
            string applicationId = newApplication.ApplicationId;

            // create job submission remote path
            string jobSubmissionDirectory =
                _jobSubmissionDirectoryProvider.GetJobSubmissionRemoteDirectory(applicationId);

            // create local driver folder.
            var localDriverFolderPath = CreateDriverFolder(jobId, applicationId);
            try
            {
                Log.Log(Level.Verbose, "Preparing driver folder in {0}", localDriverFolderPath);
                _driverFolderPreparationHelper.PrepareDriverFolder(jobSubmission, localDriverFolderPath);

                // prepare configuration
                var paramInjector = TangFactory.GetTang().NewInjector(jobSubmission.DriverConfigurations.ToArray());
                var maxApplicationSubmissions = 
                    paramInjector.GetNamedInstance<DriverBridgeConfigurationOptions.MaxApplicationSubmissions, int>();

                SerializeAppFile(jobSubmission, paramInjector, localDriverFolderPath);
                SerializeJobFile(jobSubmission, localDriverFolderPath, jobSubmissionDirectory);

                // upload prepared folder to DFS
                var jobResources = _jobResourceUploader.UploadJobResource(localDriverFolderPath, jobSubmissionDirectory);

                // submit job
                Log.Log(Level.Verbose, @"Assigned application id {0}", applicationId);

                var submissionReq = CreateApplicationSubmissionRequest(jobSubmission,
                    applicationId,
                    maxApplicationSubmissions,
                    jobResources);
                var submittedApplication = _yarnRMClient.SubmitApplicationAsync(submissionReq).GetAwaiter().GetResult();
                Log.Log(Level.Info, @"Submitted application {0}", submittedApplication.Id);
            }
            finally
            {
                if (Directory.Exists(localDriverFolderPath))
                {
                    Directory.Delete(localDriverFolderPath, recursive: true);
                }
            }
        }

        private void SerializeAppFile(IJobSubmission jobSubmission, IInjector paramInjector, string localDriverFolderPath)
        {
            var avroAppSubmissionParameters = new AvroAppSubmissionParameters
            {
                tcpBeginPort = paramInjector.GetNamedInstance<TcpPortRangeStart, int>(),
                tcpRangeCount = paramInjector.GetNamedInstance<TcpPortRangeCount, int>(),
                tcpTryCount = paramInjector.GetNamedInstance<TcpPortRangeTryCount, int>()
            };

            var avroYarnAppSubmissionParameters = new AvroYarnAppSubmissionParameters
            {
                sharedAppSubmissionParameters = avroAppSubmissionParameters,
                driverMemory = jobSubmission.DriverMemory,
                driverRecoveryTimeout =
                    paramInjector.GetNamedInstance<DriverBridgeConfigurationOptions.DriverRestartEvaluatorRecoverySeconds, int>(),
            };

            var submissionAppArgsFilePath = Path.Combine(
                localDriverFolderPath, _fileNames.GetLocalFolderPath(), _fileNames.GetSubmissionAppParametersFile());

            using (var jobArgsFileStream = new FileStream(submissionAppArgsFilePath, FileMode.CreateNew))
            {
                var serializedArgs =
                    AvroJsonSerializer<AvroYarnAppSubmissionParameters>.ToBytes(avroYarnAppSubmissionParameters);
                jobArgsFileStream.Write(serializedArgs, 0, serializedArgs.Length);
            }
        }

        private void SerializeJobFile(IJobSubmission jobSubmission, string localDriverFolderPath, string jobSubmissionDirectory)
        {
            var avroJobSubmissionParameters = new AvroJobSubmissionParameters
            {
                jobId = jobSubmission.JobIdentifier,
                jobSubmissionFolder = localDriverFolderPath
            };

            var avroYarnJobSubmissionParameters = new AvroYarnJobSubmissionParameters
            {
                jobSubmissionDirectoryPrefix = jobSubmissionDirectory,
                dfsJobSubmissionFolder = jobSubmissionDirectory,
                sharedJobSubmissionParameters = avroJobSubmissionParameters
            };

            var submissionJobArgsFilePath = Path.Combine(localDriverFolderPath,
                _fileNames.GetSubmissionJobParametersFile());

            using (var jobArgsFileStream = new FileStream(submissionJobArgsFilePath, FileMode.CreateNew))
            {
                var serializedArgs =
                    AvroJsonSerializer<AvroYarnJobSubmissionParameters>.ToBytes(avroYarnJobSubmissionParameters);
                jobArgsFileStream.Write(serializedArgs, 0, serializedArgs.Length);
            }
        }

        public IJobSubmissionResult SubmitAndGetJobStatus(IJobSubmission jobSubmission)
        {
            throw new NotSupportedException();
        }

        public async Task<FinalState> GetJobFinalStatus(string appId)
        {
            var application = await _yarnRMClient.GetApplicationAsync(appId).ConfigureAwait(false);
            return application.FinalStatus;
        }

        private SubmitApplication CreateApplicationSubmissionRequest(
           IJobSubmission jobSubmission,
           string appId,
           int maxApplicationSubmissions,
           ICollection<JobResource> jobResources)
        {
            string command = _yarnJobCommandProvider.GetJobSubmissionCommand();
            Log.Log(Level.Verbose, "Command for YARN: {0}", command);
            Log.Log(Level.Verbose, "ApplicationID: {0}", appId);
            Log.Log(Level.Verbose, "MaxApplicationSubmissions: {0}", maxApplicationSubmissions);
            foreach (var jobResource in jobResources)
            {
                Log.Log(Level.Verbose, "Remote file: {0}", jobResource.RemoteUploadPath);
            }

            var submitApplication = new SubmitApplication
            {
                ApplicationId = appId,
                ApplicationName = jobSubmission.JobIdentifier,
                AmResource = new Resouce
                {
                    MemoryMB = jobSubmission.DriverMemory,
                    VCores = 1 // keeping parity with existing code
                },
                MaxAppAttempts = maxApplicationSubmissions,
                ApplicationType = REEFApplicationType,
                KeepContainersAcrossApplicationAttempts = true,
                Queue = @"default", // keeping parity with existing code
                Priority = 1, // keeping parity with existing code
                UnmanagedAM = false,
                AmContainerSpec = new AmContainerSpec
                {
                    LocalResources = CreateLocalResources(jobResources),
                    Commands = new Commands
                    {
                        Command = command
                    }
                }
            };

            return submitApplication;
        }

        private static LocalResources CreateLocalResources(ICollection<JobResource> jobResources)
        {
            return new LocalResources
            {
                Entries = jobResources.Select(jobResource => new RestClient.DataModel.KeyValuePair<string, LocalResourcesValue>
                {
                    Key = jobResource.Name,
                    Value = new LocalResourcesValue
                    {
                        Resource = jobResource.RemoteUploadPath,
                        Type = ResourceType.ARCHIVE,
                        Visibility = Visibility.APPLICATION,
                        Size = jobResource.ResourceSize,
                        Timestamp = jobResource.LastModificationUnixTimestamp
                    }
                }).ToArray()
            };
        }

        /// <summary>
        /// Creates the temporary directory to hold the job submission.
        /// </summary>
        /// <returns>The path to the folder created.</returns>
        private static string CreateDriverFolder(string jobId, string appId)
        {
            return Path.GetFullPath(Path.Combine(Path.GetTempPath(), string.Join("-", "reef", jobId, appId)));
        }
    }
}