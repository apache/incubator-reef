/**
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

using System.Runtime.Serialization;

namespace Org.Apache.REEF.Client.Avro
{
    /// <summary>
    /// Used to serialize and deserialize Avro record org.apache.reef.reef.bridge.client.AvroBootstrapArgs.
    /// </summary>
    [DataContract(Namespace = "org.apache.reef.reef.bridge.client")]
    public sealed class AvroBootstrapArgs
    {
        private const string JsonSchema = @"{""type"":""record"",""name"":""org.apache.reef.reef.bridge.client.AvroBootstrapArgs"",""fields"":[{""name"":""jobId"",""type"":""string""},{""name"":""tcpBeginPort"",""type"":""int""},{""name"":""tcpRangeCount"",""type"":""int""},{""name"":""tcpTryCount"",""type"":""int""},{""name"":""jobSubmissionFolder"",""type"":""string""}]}";

        /// <summary>
        /// Gets the schema.
        /// </summary>
        public static string Schema
        {
            get
            {
                return JsonSchema;
            }
        }
      
        /// <summary>
        /// Gets or sets the jobId field.
        /// </summary>
        [DataMember]
        public string jobId { get; set; }

        /// <summary>
        /// Gets or sets the jobSubmissionFolder field.
        /// </summary>
        [DataMember]
        public string jobSubmissionFolder { get; set; }
              
        /// <summary>
        /// Gets or sets the tcpBeginPort field.
        /// </summary>
        [DataMember]
        public int tcpBeginPort { get; set; }
              
        /// <summary>
        /// Gets or sets the tcpRangeCount field.
        /// </summary>
        [DataMember]
        public int tcpRangeCount { get; set; }
              
        /// <summary>
        /// Gets or sets the tcpTryCount field.
        /// </summary>
        [DataMember]
        public int tcpTryCount { get; set; }
                
        /// <summary>
        /// Initializes a new instance of the <see cref="AvroBootstrapArgs"/> class.
        /// </summary>
        public AvroBootstrapArgs()
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="AvroBootstrapArgs"/> class.
        /// </summary>
        /// <param name="jobId">The jobId.</param>
        /// <param name="jobSubmissionFolder">The jobSubmissionFolder</param>
        /// <param name="tcpBeginPort">The tcpBeginPort.</param>
        /// <param name="tcpRangeCount">The tcpRangeCount.</param>
        /// <param name="tcpTryCount">The tcpTryCount.</param>
        public AvroBootstrapArgs(string jobId, string jobSubmissionFolder, int tcpBeginPort, int tcpRangeCount, int tcpTryCount)
        {
            this.jobId = jobId;
            this.jobSubmissionFolder = jobSubmissionFolder;
            this.tcpBeginPort = tcpBeginPort;
            this.tcpRangeCount = tcpRangeCount;
            this.tcpTryCount = tcpTryCount;
        }
    }
}
