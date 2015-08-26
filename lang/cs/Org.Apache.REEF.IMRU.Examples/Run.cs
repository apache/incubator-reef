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
using System.Globalization;
using System.Linq;
using Org.Apache.REEF.IMRU.Examples.PipelinedBroadcastReduce;
using Org.Apache.REEF.Tang.Implementations.Tang;
using Org.Apache.REEF.Tang.Interface;
using Org.Apache.REEF.Wake.Remote.Parameters;
using Org.Apache.REEF.Utilities.Logging;

namespace Org.Apache.REEF.IMRU.Examples
{
    /// <summary>
    /// Runs IMRU for mapper count either in localruntime or on cluster.
    /// </summary>
    public class Run
    {
        private static readonly Logger Logger = Logger.GetLogger(typeof(Run));

        private static void RunMapperTest(IConfiguration tcpPortConfig, bool runOnYarn, int numNodes)
        {
            IInjector injector;

            if (!runOnYarn)
            {
                injector =
                    TangFactory.GetTang()
                        .NewInjector(OnREEFIMRURunTimeConfiguration<int, int, int>.GetLocalIMRUConfiguration(numNodes), tcpPortConfig);
            }
            else
            {
                injector = TangFactory.GetTang()
                    .NewInjector(OnREEFIMRURunTimeConfiguration<int, int, int>.GetYarnIMRUConfiguration(), tcpPortConfig);
            }

            var mapperCountExample = injector.GetInstance<MapperCount.MapperCount>();
            mapperCountExample.Run(numNodes - 1);
        }

        private static void RunBroadcastReduceTest(IConfiguration tcpPortConfig, bool runOnYarn, int numNodes, string[] args)
        {
            int chunkSize = 2;
            int dims = 10;
            int iterations = 10;
            int mapperMemory = 512;
            int updateTaskMemory = 512;

            if (args.Length > 0)
            {
                dims = Convert.ToInt32(args[0]);
            }

            if (args.Length > 1)
            {
                chunkSize = Convert.ToInt32(args[1]);
            }

            if (args.Length > 2)
            {
                mapperMemory = Convert.ToInt32(args[2]);
            }

            if (args.Length > 3)
            {
                updateTaskMemory = Convert.ToInt32(args[3]);
            }

            if (args.Length > 4)
            {
                iterations = Convert.ToInt32(args[4]);
            }

            IInjector injector;

            if (!runOnYarn)
            {
                injector =
                    TangFactory.GetTang()
                        .NewInjector(OnREEFIMRURunTimeConfiguration<int[], int[], int[]>.GetLocalIMRUConfiguration(numNodes), tcpPortConfig);
            }
            else
            {
                injector = TangFactory.GetTang()
                    .NewInjector(OnREEFIMRURunTimeConfiguration<int[], int[], int[]>.GetYarnIMRUConfiguration(), tcpPortConfig);
            }
            var broadcastReduceExample = injector.GetInstance<PipelinedBroadcastAndReduce>();
            broadcastReduceExample.Run(numNodes - 1, chunkSize, iterations, dims, mapperMemory, updateTaskMemory);
        }

        private static void Main(string[] args)
        {
            Logger.Log(Level.Info, "start running client: " + DateTime.Now);
            string methodName = "MapperCount";
            bool runOnYarn = false;
            int numNodes = 2;
            int startPort = 8900;
            int portRange = 1000;

            if (args != null)
            {
                if (args.Length > 0)
                {
                    runOnYarn = bool.Parse(args[0].ToLower());
                }

                if (args.Length > 1)
                {
                    numNodes = int.Parse(args[1]);
                }

                if (args.Length > 2)
                {
                    startPort = int.Parse(args[2]);
                }

                if (args.Length > 3)
                {
                    portRange = int.Parse(args[3]);
                }

                if (args.Length > 4)
                {
                    methodName = args[4];
                }
            }

            var tcpPortConfig = TangFactory.GetTang().NewConfigurationBuilder()
                .BindNamedParameter(typeof(TcpPortRangeStart),
                    startPort.ToString(CultureInfo.InvariantCulture))
                .BindNamedParameter(typeof(TcpPortRangeCount),
                    portRange.ToString(CultureInfo.InvariantCulture))
                .Build();

            switch (methodName.ToLower())
            {
                case "mappercount":
                    Logger.Log(Level.Info, "Running Mapper count");
                    RunMapperTest(tcpPortConfig, runOnYarn, numNodes);
                    Logger.Log(Level.Info, "Done Running Mapper count");
                    return;

                case "broadcastandreduce":
                    Logger.Log(Level.Info, "Running Broadcast and Reduce");
                    RunBroadcastReduceTest(tcpPortConfig, runOnYarn, numNodes, args.Skip(5).ToArray());
                    Logger.Log(Level.Info, "Done Running Broadcast and Reduce");
                    return;

                default:
                    Logger.Log(Level.Info, "wrong test name");
                    return;

            }
        }
    }
}
