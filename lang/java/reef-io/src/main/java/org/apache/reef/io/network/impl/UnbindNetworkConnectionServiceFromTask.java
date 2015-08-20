/*
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
package org.apache.reef.io.network.impl;

import org.apache.reef.io.network.NetworkConnectionService;
import org.apache.reef.io.network.impl.config.NetworkConnectionServiceIdFactory;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.task.events.TaskStop;
import org.apache.reef.wake.EventHandler;
import org.apache.reef.wake.IdentifierFactory;

import javax.inject.Inject;
/**
 * TaskStop event handler for unregistering NetworkConnectionService.
 * Users have to bind this handler into ServiceConfiguration.ON_TASK_STOP.
 * @deprecated in 0.13. Users should register/unregister an end point id by themselves.
 */
@Deprecated
public final class UnbindNetworkConnectionServiceFromTask implements EventHandler<TaskStop> {

  private final NetworkConnectionService ncs;
  private final IdentifierFactory idFac;

  @Inject
  public UnbindNetworkConnectionServiceFromTask(
      final NetworkConnectionService ncs,
      @Parameter(NetworkConnectionServiceIdFactory.class) final IdentifierFactory idFac) {
    this.ncs = ncs;
    this.idFac = idFac;
  }

  @Override
  public void onNext(final TaskStop task) {
    this.ncs.unregisterId(this.idFac.getNewInstance(task.getId()));
  }
}
