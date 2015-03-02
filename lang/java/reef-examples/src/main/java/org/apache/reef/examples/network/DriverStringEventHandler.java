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
package org.apache.reef.examples.network;

import org.apache.reef.io.network.NetworkEvent;
import org.apache.reef.io.network.NetworkEventHandler;
import org.apache.reef.io.network.NetworkService;
import org.apache.reef.tang.InjectionFuture;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responses when OddIntegerEventTask sends a message
 */
public class DriverStringEventHandler implements NetworkEventHandler<String> {

  private static final Logger LOG = Logger.getLogger(OddIntegerEventHandler.class.getName());

  private InjectionFuture<NetworkService> networkService;

  @Inject
  public DriverStringEventHandler(
      final InjectionFuture<NetworkService> networkService) {
    this.networkService = networkService;
  }

  @Override
  public void onNext(NetworkEvent<String> value) {
    LOG.log(Level.INFO, "{0}", value);
    networkService.get().sendEvent(value.getRemoteId(), "World!");
  }
}

