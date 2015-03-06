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
using System.Collections.Generic;

namespace Org.Apache.REEF.Tang.Interface
{
    public interface ITang
    {
        IInjector NewInjector();
        IInjector NewInjector(IConfiguration[] confs);
        IInjector NewInjector(IConfiguration confs);
        IInjector NewInjector(string[] assemblies, string configurationFileName);
        IInjector NewInjector(string[] assemblies, IDictionary<string, string> configurations);
        IInjector NewInjector(string[] assemblies, IList<KeyValuePair<string, string>> configurations);
        IClassHierarchy GetClassHierarchy(string[] assemblies);
        ICsClassHierarchy GetDefaultClassHierarchy();
        ICsClassHierarchy GetDefaultClassHierarchy(string[] assemblies, Type[] parameterParsers);

        ICsConfigurationBuilder NewConfigurationBuilder();
        ICsConfigurationBuilder NewConfigurationBuilder(string[] assemblies);
        ICsConfigurationBuilder NewConfigurationBuilder(IConfiguration[] confs);
        ICsConfigurationBuilder NewConfigurationBuilder(IConfiguration conf);
        ICsConfigurationBuilder NewConfigurationBuilder(string[] assemblies, IConfiguration[] confs, Type[] parameterParsers);
        IConfigurationBuilder NewConfigurationBuilder(IClassHierarchy classHierarchy);
        ICsConfigurationBuilder NewConfigurationBuilder(ICsClassHierarchy classHierarchy);

        ICsConfigurationBuilder NewConfigurationBuilder(Type[] parameterParsers);
    }
}
