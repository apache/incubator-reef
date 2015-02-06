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
using Org.Apache.REEF.Tang.Implementations;
using Org.Apache.REEF.Tang.Interface;
using Org.Apache.REEF.Tang.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Org.Apache.REEF.Tang.Implementations.Tang;

namespace Org.Apache.REEF.Tang.Tests.ScenarioTest
{
    [TestClass]
    public class TestTrackingURIProvider
    {
        [TestMethod]
        public void TrackingIdThroughNamedParameterTest()
        {
            ICsConfigurationBuilder cba = TangFactory.GetTang().NewConfigurationBuilder();
            string trackingId = System.Environment.MachineName + ":8080";
            cba.BindNamedParameter<TrackingId, string>(GenericType<TrackingId>.Class, trackingId);
            string id = (string)TangFactory.GetTang().NewInjector(cba.Build()).GetNamedInstance(typeof(TrackingId));
            Assert.AreEqual(id, trackingId);
        }

        [TestMethod]
        public void DefaultTrackingIdThroughInterfaceTest()
        {
            string trackingId = System.Environment.MachineName + ":8080";
            var id = TangFactory.GetTang().NewInjector().GetInstance<ITrackingURIProvider>();
            Assert.AreEqual(id.GetURI(), trackingId);
        }

        [TestMethod]
        public void TrackingIdThroughInterfaceTest()
        {
            ICsConfigurationBuilder cba = TangFactory.GetTang().NewConfigurationBuilder();
            cba.BindNamedParameter<PortNumber, string>(GenericType<PortNumber>.Class, "8080");
            string trackingId = System.Environment.MachineName + ":8080";
            var id = TangFactory.GetTang().NewInjector().GetInstance<ITrackingURIProvider>();
            Assert.AreEqual(id.GetURI(), trackingId);
        }
    }
}