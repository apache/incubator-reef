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

using System.Collections.Generic;

namespace Org.Apache.REEF.IO.PartitionedData.FileSystem
{
    /// <summary>
    /// A interface for user to implement its deserializer.
    /// The input is a list of file names.
    /// The out put is an IEnumerable of T which is defined by the client
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public interface IFileSerializer<T>
    {
        IEnumerable<T> Deserialize(IList<string> filePaths);
    }
}
