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

using Org.Apache.REEF.Network.Naming.Events;
using Org.Apache.REEF.Wake.RX;

namespace Org.Apache.REEF.Network.Naming.Observers
{
    /// <summary>
    /// Handler for unregistering an identifier with the NameServer
    /// </summary>
    internal class NamingUnregisterRequestObserver : AbstractObserver<NamingUnregisterRequest>
    {
        private NameServer _server;

        public NamingUnregisterRequestObserver(NameServer server)
        {
            _server = server;
        }

        /// <summary>
        /// Unregister the identifer with the NameServer.  
        /// </summary>
        /// <param name="value">The unregister request event</param>
        public override void OnNext(NamingUnregisterRequest value)
        {
            // Don't send a response
            _server.Unregister(value.Identifier); 
        }
    }
}
