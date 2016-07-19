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
using System.IO;
using Org.Apache.REEF.IO.PartitionedData.FileSystem;
using Org.Apache.REEF.Tang.Annotations;

namespace Org.Apache.REEF.Demo.Task
{
    public class ByteSerializer : IFileDeSerializer<IEnumerable<byte>>
    {
        [Inject]
        public ByteSerializer()
        {
        }

        public IEnumerable<byte> Deserialize(ISet<string> filePaths)
        {
            foreach (var f in filePaths)
            {
                using (FileStream stream = File.Open(f, FileMode.Open))
                {
                    BinaryReader reader = new BinaryReader(stream);
                    while (reader.PeekChar() != -1)
                    {
                        yield return reader.ReadByte();
                    }
                }
            }
        }
    }
}
