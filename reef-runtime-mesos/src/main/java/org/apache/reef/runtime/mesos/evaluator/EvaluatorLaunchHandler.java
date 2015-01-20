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
package org.apache.reef.runtime.mesos.evaluator;

import org.apache.reef.annotations.audience.EvaluatorSide;
import org.apache.reef.annotations.audience.Private;
import org.apache.reef.runtime.mesos.proto.ReefRuntimeMesosProtocol.EvaluatorLaunchProto;
import org.apache.reef.tang.InjectionFuture;
import org.apache.reef.wake.EventHandler;
import org.apache.reef.wake.remote.RemoteMessage;

import javax.inject.Inject;

/**
 * Handles evaluator launch requests via MesosRemoteManager from MesosResourceLaunchHandler
 */
@EvaluatorSide
@Private
final class EvaluatorLaunchHandler implements EventHandler<RemoteMessage<EvaluatorLaunchProto>> {
  // EvaluatorLaunchHandler is registered in MesosExecutor. Hence, we need an InjectionFuture here.
  private final InjectionFuture<REEFExecutor> mesosExecutor;

  @Inject
  EvaluatorLaunchHandler(final InjectionFuture<REEFExecutor> mesosExecutor) {
    this.mesosExecutor = mesosExecutor;
  }

  @Override
  public void onNext(final RemoteMessage<EvaluatorLaunchProto> remoteMessage) {
    this.mesosExecutor.get().onEvaluatorLaunch(remoteMessage.getMessage());
  }
}