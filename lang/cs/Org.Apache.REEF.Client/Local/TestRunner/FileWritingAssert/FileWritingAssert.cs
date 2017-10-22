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
using System.IO;
using Org.Apache.REEF.Tang.Annotations;
using Org.Apache.REEF.Client.API.Testing;

namespace Org.Apache.REEF.Client.Local.TestRunner.FileWritingAssert
{
    internal sealed class FileWritingAssert : AbstractAssert
    {
        private readonly TestResult _testResult = new TestResult();
        private readonly string _filePath;

        /// <param name="filePath">The path to the file where the assert results shall be written.</param>
        [Inject]
        internal FileWritingAssert([Parameter(typeof(Parameters.AssertFilePath))] string filePath)
        {
            _filePath = filePath;
        }

        public override void True(string message, bool condition)
        {
            _testResult.RecordAssertResult(message, condition);
            WriteAssertsFile();
        }

        internal TestResult TestResult
        {
            get
            {
                return _testResult;
            }
        }

        internal static TestResult ReadTestResultsFromFile(string fileName)
        {
            return TestResult.FromJson(fileName);
        }

        private void WriteAssertsFile()
        {
            File.WriteAllText(_filePath, _testResult.ToJson());
        }
    }
}
