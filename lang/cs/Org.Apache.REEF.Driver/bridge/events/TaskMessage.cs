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

using Org.Apache.REEF.Driver.Task;
using System;
using System.Runtime.Serialization;

namespace Org.Apache.REEF.Driver.Bridge
{
    /// <summary>
    /// TaskMessage which wraps ITaskMessageClr2Java
    /// </summary>
    [DataContract]
    internal class TaskMessage : ITaskMessage
    {
        private ITaskMessageClr2Java _taskMessageClr2Java;
        private byte[] _message;
        private string _instanceId;

        public TaskMessage(ITaskMessageClr2Java clr2Java, byte[] message)
        {
            _instanceId = Guid.NewGuid().ToString("N");
            _taskMessageClr2Java = clr2Java;
            _message = message;
        }

        [DataMember]
        public string InstanceId
        {
            get { return _instanceId; }
            set { _instanceId = value; }
        }

        [DataMember]
        public string TaskId
        {
            get { return _taskMessageClr2Java.GetId(); }
            set { }
        }

        [DataMember]
        public byte[] Message
        {
            get { return _message; }
            set { _message = value; } 
        }
    }
}
