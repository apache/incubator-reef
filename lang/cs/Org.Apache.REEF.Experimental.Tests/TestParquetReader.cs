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

using System.IO;
using System.Runtime.Serialization;
using Org.Apache.REEF.Experimental.ParquetReader;
using Org.Apache.REEF.Tang.Implementations.Tang;
using Org.Apache.REEF.Tang.Interface;
using Org.Apache.REEF.Tang.Util;
using Xunit;

namespace Org.Apache.REEF.Experimental.Tests
{
    public sealed class TestParquetReader
    {
        [DataContract(Name = "User", Namespace = "parquetreader")]
        internal class User
        {
            [DataMember(Name = "name")]
            public string name { get; set; }

            [DataMember(Name = "age")]
            public int age { get; set; }

            [DataMember(Name = "favorite_color")]
            public string favorite_color { get; set; }
        }

        [Fact]
        public void TestReadParquetFile()
        {
            // Identify parquet file and jar path
            var dir = new DirectoryInfo(Directory.GetCurrentDirectory());
            while (!dir.FullName.EndsWith(@"reef\lang"))
            {
                dir = Directory.GetParent(dir.FullName);
            }
            var parquetPath = dir.FullName + @"\java\reef-experimental\src\test\resources\file.parquet";
            var jarPath = Directory.GetFiles(dir.FullName + @"\java\reef-experimental\target\", "*jar-with-dependencies.jar")[0];

            Assert.True(File.Exists(parquetPath));
            Assert.True(File.Exists(jarPath));

            System.Diagnostics.Debug.WriteLine("Parquet Path: {0}", parquetPath);
            System.Diagnostics.Debug.WriteLine("Jar Path: {0}", jarPath);

            ITang tang = TangFactory.GetTang();
            IConfiguration conf = tang.NewConfigurationBuilder()
              .BindNamedParameter<ParquetPathString, string>(GenericType<ParquetPathString>.Class, parquetPath)
              .BindNamedParameter<JarPathString, string>(GenericType<JarPathString>.Class, jarPath)
              .Build();
            IInjector injector = tang.NewInjector(conf);

            var reader = injector.GetInstance<ParquetReader.ParquetReader>();
            var i = 0;
            foreach (var obj in reader.Read<User>())
            {
                Assert.Equal(obj.name, "User_" + i);
                Assert.Equal(obj.age, i);
                Assert.Equal(obj.favorite_color, "blue");
                System.Diagnostics.Debug.WriteLine(
                    "Object(name: {0}, age: {1}, favorite_color: {2})", 
                    obj.name, obj.age, obj.favorite_color);
                i++;
            }

            reader.Dispose();
            Assert.Equal(i, 10);
        }
    }
}
