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
package org.apache.reef.vortex.driver;

import org.apache.reef.annotations.Unstable;
import org.apache.reef.annotations.audience.DriverSide;
import org.apache.reef.tang.annotations.Name;
import org.apache.reef.tang.annotations.NamedParameter;
import org.apache.reef.tang.formats.ConfigurationModule;
import org.apache.reef.tang.formats.ConfigurationModuleBuilder;
import org.apache.reef.tang.formats.RequiredImpl;
import org.apache.reef.tang.formats.RequiredParameter;
import org.apache.reef.vortex.api.VortexStart;
import org.apache.reef.wake.EStage;
import org.apache.reef.wake.StageConfiguration;
import org.apache.reef.wake.impl.ThreadPoolStage;

/**
 * Vortex Master configurations.
 */
@Unstable
@DriverSide
public final class VortexMasterConf extends ConfigurationModuleBuilder {
  /**
   * Number of Workers.
   */
  @NamedParameter(doc = "Number of Workers")
  public final class WorkerNum implements Name<Integer> {
  }

  /**
   * Worker Memory.
   */
  @NamedParameter(doc = "Worker Memory")
  public final class WorkerMem implements Name<Integer> {
  }

  /**
   * Worker Cores.
   */
  @NamedParameter(doc = "Worker Cores")
  public final class WorkerCores implements Name<Integer> {
  }

  public static final RequiredParameter<Integer> WORKER_NUM = new RequiredParameter<>();
  public static final RequiredParameter<Integer> WORKER_MEM = new RequiredParameter<>();
  public static final RequiredParameter<Integer> WORKER_CORES = new RequiredParameter<>();
  public static final RequiredImpl<VortexStart> VORTEX_START = new RequiredImpl<>();
  public static final RequiredParameter<Integer> NUM_OF_VORTEX_START_THERAD = new RequiredParameter<>();

  public static final ConfigurationModule CONF = new VortexMasterConf()
      .bindNamedParameter(WorkerNum.class, WORKER_NUM)
      .bindNamedParameter(WorkerMem.class, WORKER_MEM)
      .bindNamedParameter(WorkerCores.class, WORKER_CORES)
      .bindImplementation(VortexStart.class, VORTEX_START)
      .bindNamedParameter(StageConfiguration.NumberOfThreads.class, NUM_OF_VORTEX_START_THERAD)
      .bindNamedParameter(StageConfiguration.StageHandler.class, VortexStartExecutor.class)
      .bindImplementation(EStage.class, ThreadPoolStage.class)
      .build();
}
