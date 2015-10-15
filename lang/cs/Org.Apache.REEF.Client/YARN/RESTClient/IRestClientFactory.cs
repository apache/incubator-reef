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
using Org.Apache.REEF.Tang.Annotations;
using RestSharp;

namespace Org.Apache.REEF.Client.Yarn.RestClient
{
    [DefaultImplementation(typeof(RestClientFactory))]
    internal interface IRestClientFactory
    {
        IRestClient CreateRestClient(Uri baseUri);
    }

    internal class RestClientFactory : IRestClientFactory
    {
        [Inject]
        private RestClientFactory()
        {
        }

        public IRestClient CreateRestClient(Uri baseUri)
        {
            // TODO: We are creating a new client per request
            // as one client can contact only one baseUri.
            // This is not very bad but it might still be worth
            // it to cache clients per baseUri in the future.
            return new RestSharp.RestClient(baseUri)
            {
                FollowRedirects = true
            };
        }
    }
}