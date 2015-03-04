﻿/**
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

using Org.Apache.REEF.Wake.Remote;
using Org.Apache.REEF.Network.Group.Pipelining;

namespace Org.Apache.REEF.Network.Group.Operators.Impl
{
    /// <summary>
    /// The specification used to define Pipelined Scatter MPI Operators.
    /// </summary>
    public class PipelinedScatterOperatorSpec<T> : ScatterOperatorSpec<T>
    {
        /// <summary>
        /// Creates a new ScatterOperatorSpec.
        /// </summary>
        /// <param name="senderId">The identifier of the task that will
        /// be sending messages</param>
        /// <param name="codec">The codec used to serialize and 
        /// deserialize messages</param>
        /// <param name="dataConverter">The converter used to convert original 
        /// message to pipelined ones and vice versa.</param>
        public PipelinedScatterOperatorSpec(
            string senderId, 
            ICodec<T> codec,
            IPipelineDataConverter<T> dataConverter)
            : base(senderId, codec)
        {
            PipelineDataConverter = dataConverter;
        }

        /// <summary>
        /// Returns the IPipelineDataConvert used to convert messages to pipeline form and vice-versa
        /// </summary>
        public IPipelineDataConverter<T> PipelineDataConverter { get; private set; }
    }
}
