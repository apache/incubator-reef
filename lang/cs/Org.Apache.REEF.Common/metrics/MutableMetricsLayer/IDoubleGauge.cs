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

using Org.Apache.REEF.Utilities.Attributes;

namespace Org.Apache.REEF.Common.Metrics.MutableMetricsLayer
{
    /// <summary>
    /// Interface to implement the double gauge. A user will only work against this interface. 
    /// Implementations will be internal.
    /// </summary>
    [Unstable("0.16", "Contract may change.")]
    public interface IDoubleGauge : IMutableMetric
    {
        /// <summary>
        /// Increments the gauge by 1.
        /// </summary>
        void Increment();

        /// <summary>
        /// Increments the gauge by delta.
        /// </summary>
        /// <param name="delta">Value with which to increment.</param>
        void Increment(double delta);

        /// <summary>
        /// Decrements the gauge by 1.
        /// </summary>
        void Decrement();

        /// <summary>
        /// Decrements the gauge by delta.
        /// </summary>
        /// <param name="delta">Value with which to increment.</param>
        void Decrement(double delta);

        /// <summary>
        /// Resets the gauge.
        /// </summary>
        /// <param name="value">Value to which the gauge should be reset.</param>
        void Reset(double value);
    }
}
