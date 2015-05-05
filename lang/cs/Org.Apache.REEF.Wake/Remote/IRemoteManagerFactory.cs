﻿/*
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

using System.Net;
using Org.Apache.REEF.Tang.Annotations;
using Org.Apache.REEF.Wake.Impl;

namespace Org.Apache.REEF.Wake.Remote
{
    /// <summary>
    /// Creates new instances of IRemoteManager.
    /// </summary>
    [DefaultImplementation(typeof(DefaultRemoteManagerFactory))]
    public interface IRemoteManagerFactory
    {
        /// <summary>
        /// Constructs a DefaultRemoteManager listening on the specified address and any
        /// available port.
        /// </summary>
        /// <param name="localAddress">The address to listen on</param>
        /// <param name="port">The port to listen on</param>
        /// <param name="codec">The codec used for serializing messages</param>
        /// <param name="tcpPortProvider">Provides ports for tcp listeners.</param>
        IRemoteManager<T> GetInstance<T>(IPAddress localAddress, int port, ICodec<T> codec, ITcpPortProvider tcpPortProvider);

        /// <summary>
        /// Constructs a DefaultRemoteManager. Does not listen for incoming messages.
        /// </summary>
        /// <param name="codec">The codec used for serializing messages</param>
        IRemoteManager<T> GetInstance<T>(ICodec<T> codec);
    }
}