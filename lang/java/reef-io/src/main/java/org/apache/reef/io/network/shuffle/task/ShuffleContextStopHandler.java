/**
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
package org.apache.reef.io.network.shuffle.task;

import org.apache.reef.evaluator.context.events.ContextStop;
import org.apache.reef.io.network.NetworkServiceClient;
import org.apache.reef.io.network.shuffle.params.ShuffleControlMessageNSId;
import org.apache.reef.wake.EventHandler;

import javax.inject.Inject;

/**
 *
 */
public final class ShuffleContextStopHandler implements EventHandler<ContextStop> {

  private final NetworkServiceClient nsClient;

  @Inject
  public ShuffleContextStopHandler(
      final NetworkServiceClient nsClient) {

    this.nsClient = nsClient;
  }

  @Override
  public void onNext(ContextStop value) {
    nsClient.unregisterConnectionFactory(ShuffleControlMessageNSId.class);
  }
}
