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

using System;
using Org.Apache.REEF.Tang.Interface;

namespace Org.Apache.REEF.Common.Task
{
    public class TaskClientCodeException : Exception
    {
        private readonly string _taskId;

        private readonly string _contextId;

        /// <summary>
        /// construct the exception that caused the Task to fail
        /// </summary>
        /// <param name="taskId"> the id of the failed task.</param>
        /// <param name="contextId"> the id of the context the failed Task was executing in.</param>
        /// <param name="message"> the error message </param>
        /// <param name="cause"> the exception that caused the Task to fail.</param>
        public TaskClientCodeException(
                string taskId,
                string contextId,
                string message,
                Exception cause)
            : base(message, cause)
        {
            _taskId = taskId;
            _contextId = contextId;
        }

        public string TaskId 
        {
            get { return _taskId; }
        }

        public string ContextId
        {
            get { return _contextId; }
        }

        public static string GetTaskIdentifier(IConfiguration c)
        {
            // TODO: update after TANG is available
            return string.Empty;
        }
    }
}
