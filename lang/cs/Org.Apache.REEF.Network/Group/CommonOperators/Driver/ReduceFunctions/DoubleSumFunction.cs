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
using System.Linq;
using Org.Apache.REEF.Network.Group.Operators;
using Org.Apache.REEF.Tang.Annotations;

namespace Org.Apache.REEF.Network.Group.CommonOperators.Driver.ReduceFunctions
{
    /// <summary>
    /// Used to create Sum Reduce function on double to be used by Reduce operator
    /// </summary>
    public class DoubleSumFunction : IReduceFunction<double>
    {
        [Inject]
        public DoubleSumFunction()
        {
        }

        /// <summary>
        /// The sum function over doubles
        /// </summary>
        /// <param name="elements"> enumerator over the integers that we want 
        /// to reduce</param>
        /// <returns>The merged array</returns>
        public double Reduce(IEnumerable<double> elements)
        {
            return elements.Sum();
        }
    }
}
