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

using System.Collections.Generic;
using System.Globalization;
using Org.Apache.REEF.Client.API;
using Org.Apache.REEF.Driver;
using Org.Apache.REEF.IMRU.API;
using Org.Apache.REEF.IMRU.OnREEF.Driver;
using Org.Apache.REEF.IMRU.OnREEF.Parameters;
using Org.Apache.REEF.Network.Group.Config;
using Org.Apache.REEF.Tang.Annotations;
using Org.Apache.REEF.Tang.Formats;
using Org.Apache.REEF.Tang.Implementations.Configuration;
using Org.Apache.REEF.Tang.Implementations.Tang;
using Org.Apache.REEF.Tang.Interface;
using Org.Apache.REEF.Tang.Util;

namespace Org.Apache.REEF.IMRU.OnREEF.Client
{
    /// <summary>
    /// Implements the IMRU client API on REEF.
    /// </summary>
    /// <typeparam name="TMapInput">The type of the side information provided to the Map function</typeparam>
    /// <typeparam name="TMapOutput">The return type of the Map function</typeparam>
    /// <typeparam name="TResult">The return type of the computation.</typeparam>
    public sealed class REEFIMRUClient<TMapInput, TMapOutput, TResult> : IIMRUClient<TMapInput, TMapOutput, TResult>
    {
        private readonly IREEFClient _reefClient;
        private readonly JobSubmissionBuilderFactory _jobSubmissionBuilderFactory;
        private readonly AvroConfigurationSerializer _configurationSerializer;

        [Inject]
        private REEFIMRUClient(IREEFClient reefClient, AvroConfigurationSerializer configurationSerializer, JobSubmissionBuilderFactory jobSubmissionBuilderFactory)
        {
            _reefClient = reefClient;
            _configurationSerializer = configurationSerializer;
            _jobSubmissionBuilderFactory = jobSubmissionBuilderFactory;
        }

        /// <summary>
        /// Submits the job to reefClient
        /// </summary>
        /// <param name="jobDefinition">IMRU job definition given by the user</param>
        /// <returns></returns>
        public IEnumerable<TResult> Submit(IMRUJobDefinition jobDefinition)
        {
            const string driverId = "BroadcastReduceDriver";

            // The driver configuration contains all the needed bindings.
            var imruDriverConfiguration = TangFactory.GetTang().NewConfigurationBuilder(new[]
            {
                DriverConfiguration.ConfigurationModule
                    .Set(DriverConfiguration.OnEvaluatorAllocated,
                        GenericType<IMRUDriver<TMapInput, TMapOutput, TResult>>.Class)
                    .Set(DriverConfiguration.OnDriverStarted,
                        GenericType<IMRUDriver<TMapInput, TMapOutput, TResult>>.Class)
                    .Set(DriverConfiguration.OnContextActive,
                        GenericType<IMRUDriver<TMapInput, TMapOutput, TResult>>.Class)
                    .Set(DriverConfiguration.OnTaskCompleted,
                        GenericType<IMRUDriver<TMapInput, TMapOutput, TResult>>.Class)
                    .Build(),
                jobDefinition.PartitionedDatasetConfgiuration
            })
                .BindNamedParameter(typeof(SerializedMapConfiguration),
                    _configurationSerializer.ToString(jobDefinition.MapFunctionConfiguration))
                .BindNamedParameter(typeof(SerializedUpdateConfiguration),
                    _configurationSerializer.ToString(jobDefinition.UpdateFunctionConfiguration))
                .BindNamedParameter(typeof(SerializedMapInputCodecConfiguration),
                    _configurationSerializer.ToString(jobDefinition.MapInputCodecConfiguration))
                .BindNamedParameter(typeof(SerializedMapInputPipelineDataConverterConfiguration),
                    _configurationSerializer.ToString(jobDefinition.MapInputPipelineDataConverterConfiguration))
                .BindNamedParameter(typeof(SerializedUpdateFunctionCodecsConfiguration),
                    _configurationSerializer.ToString(jobDefinition.UpdateFunctionCodecsConfiguration))
                .BindNamedParameter(typeof(SerializedMapOutputPipelineDataConverterConfiguration),
                    _configurationSerializer.ToString(jobDefinition.MapOutputPipelineDataConverterConfiguration))
                .BindNamedParameter(typeof(SerializedReduceConfiguration),
                    _configurationSerializer.ToString(jobDefinition.ReduceFunctionConfiguration))
                .Build();

            IConfiguration groupCommDriverConfig = TangFactory.GetTang().NewConfigurationBuilder()
                .BindStringNamedParam<GroupCommConfigurationOptions.DriverId>(driverId)
                .BindStringNamedParam<GroupCommConfigurationOptions.MasterTaskId>(IMRUConstants.UpdateTaskName)
                .BindStringNamedParam<GroupCommConfigurationOptions.GroupName>(IMRUConstants.CommunicationGroupName)
                .BindIntNamedParam<GroupCommConfigurationOptions.FanOut>(
                    IMRUConstants.TreeFanout.ToString(CultureInfo.InvariantCulture).ToString(CultureInfo.InvariantCulture))
                .BindIntNamedParam<GroupCommConfigurationOptions.NumberOfTasks>(
                    (jobDefinition.NumberOfMappers + 1).ToString(CultureInfo.InvariantCulture))
                .Build();

            imruDriverConfiguration = Configurations.Merge(imruDriverConfiguration, groupCommDriverConfig);

            // The JobSubmission contains the Driver configuration as well as the files needed on the Driver.
            var imruJobSubmission = _jobSubmissionBuilderFactory.GetJobSubmissionBuilder()
                .AddDriverConfiguration(imruDriverConfiguration)
                .AddGlobalAssemblyForType(typeof(IMRUDriver<TMapInput, TMapOutput, TResult>))
                .SetJobIdentifier(jobDefinition.JobName)
                .Build();

            _reefClient.Submit(imruJobSubmission);

            return null;
        }
    }
}