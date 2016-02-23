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
package org.apache.reef.runtime.multi.driver;

import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.reef.runtime.common.driver.api.*;
import org.apache.reef.runtime.common.driver.resourcemanager.NodeDescriptorEvent;
import org.apache.reef.runtime.common.driver.resourcemanager.ResourceAllocationEvent;
import org.apache.reef.runtime.common.driver.resourcemanager.ResourceStatusEvent;
import org.apache.reef.runtime.common.driver.resourcemanager.RuntimeStatusEvent;
import org.apache.reef.runtime.multi.client.parameters.SerializedRuntimeDefinitions;
import org.apache.reef.runtime.multi.utils.RuntimeDefinition;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Injector;
import org.apache.reef.tang.Tang;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.tang.exceptions.InjectionException;
import org.apache.reef.tang.formats.AvroConfigurationSerializer;
import org.apache.reef.wake.EventHandler;
import org.apache.reef.wake.time.runtime.event.RuntimeStart;
import org.apache.reef.wake.time.runtime.event.RuntimeStop;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Hosts the actual runtime implementations and delegates invocations to them.
 */
public class RuntimesHost {
  private final Set<String> runtimeDefinitions;
  private Map<String, Injector> runtimesInjectors;
  private final Injector originalInjector;
  private String defaultRuntimeName;

  @Inject
  public RuntimesHost(final Injector injector,
                      @Parameter(SerializedRuntimeDefinitions.class) final Set<String> runtimeDefinitions) {
    if (runtimeDefinitions == null || runtimeDefinitions.size() == 0) {
      throw new RuntimeException("No runtime configurations are provided for multi-runtime");
    }

    this.runtimeDefinitions = runtimeDefinitions;
    this.originalInjector = injector;
  }

  private synchronized void initialize() {
    if (this.runtimesInjectors != null) {
      return;
    }

    this.runtimesInjectors = new HashMap<>();
    AvroConfigurationSerializer serializer = new AvroConfigurationSerializer();

    for (String serializedRuntimeDefinition : runtimeDefinitions) {

      Configuration config = null;
      RuntimeDefinition rd = null;
      try {
        rd = parseSerializedRuntimeDefinition(serializedRuntimeDefinition);

        if (rd.getDefaultConfiguration()) {
          this.defaultRuntimeName = rd.getRuntimeName().toString();
        }

        config = serializer.fromString(rd.getSerializedConfiguration().toString());
      } catch (IOException e) {
        throw new RuntimeException("Unable to read runtime configuarion.", e);
      }

      Injector runtimeInjector = Tang.Factory.getTang().newInjector();
      try {
        initializeInjector(runtimeInjector);
      } catch (InjectionException e) {
        throw new RuntimeException("Unable to initialize runtimes.", e);
      }

      runtimeInjector = runtimeInjector.forkInjector(config);
      this.runtimesInjectors.put(rd.getRuntimeName().toString(), runtimeInjector);
    }
  }

  private void initializeInjector(final Injector runtimeInjector) throws InjectionException {
    EventHandler<ResourceStatusEvent> statusEventHandler =
            this.originalInjector.getNamedInstance(RuntimeParameters.ResourceStatusHandler.class);
    runtimeInjector.bindVolatileParameter(RuntimeParameters.ResourceStatusHandler.class, statusEventHandler);
    EventHandler<NodeDescriptorEvent> nodeDescriptorEventHandler =
            this.originalInjector.getNamedInstance(RuntimeParameters.NodeDescriptorHandler.class);
    runtimeInjector.bindVolatileParameter(RuntimeParameters.NodeDescriptorHandler.class, nodeDescriptorEventHandler);
    EventHandler<ResourceAllocationEvent> resourceAllocationEventHandler =
            this.originalInjector.getNamedInstance(RuntimeParameters.ResourceAllocationHandler.class);
    runtimeInjector.bindVolatileParameter(
            RuntimeParameters.ResourceAllocationHandler.class,
            resourceAllocationEventHandler);
    EventHandler<RuntimeStatusEvent> runtimeStatusEventHandler =
            this.originalInjector.getNamedInstance(RuntimeParameters.RuntimeStatusHandler.class);
    runtimeInjector.bindVolatileParameter(
            RuntimeParameters.RuntimeStatusHandler.class,
            runtimeStatusEventHandler);
  }

  private RuntimeDefinition parseSerializedRuntimeDefinition(final String serializedRuntimeDefinition) throws
          IOException {
    RuntimeDefinition rd;
    final JsonDecoder decoder = DecoderFactory.get().
            jsonDecoder(RuntimeDefinition.getClassSchema(), serializedRuntimeDefinition);
    final SpecificDatumReader<RuntimeDefinition> reader = new SpecificDatumReader<>(RuntimeDefinition.class);
    rd = reader.read(null, decoder);
    return rd;
  }

  private Injector getInjector(final String requestedRuntimeName) {
    String runtimeName = requestedRuntimeName;
    if (runtimeName.isEmpty()) {
      runtimeName = defaultRuntimeName;
    }

    return this.runtimesInjectors.get(runtimeName);
  }

  public void onNext(final ResourceLaunchEvent value) {
    try {
      System.err.println("Resource launch called for runtime " + value.getRuntimeName());
      getInjector(value.getRuntimeName()).getInstance(ResourceLaunchHandler.class).onNext(value);
    } catch (InjectionException e) {
      e.printStackTrace();
    }
  }

  public void onNext(final RuntimeStart value) {
    System.err.println("Runtime start called for runtime ");
    initialize();
    try {
      for (Injector runtimeInjector : this.runtimesInjectors.values()) {
        runtimeInjector.getInstance(ResourceManagerStartHandler.class).onNext(value);
      }
    } catch (InjectionException e) {
      e.printStackTrace();
    }
  }

  public void onNext(final RuntimeStop value) {
    System.err.println("Runtime stop called for runtime ");
    try {
      for (Injector runtimeInjector : this.runtimesInjectors.values()) {
        runtimeInjector.getInstance(ResourceManagerStopHandler.class).onNext(value);
      }
    } catch (InjectionException e) {
      e.printStackTrace();
    }
  }

  public void onNext(final ResourceReleaseEvent value) {
    try {
      System.err.println("Resource release called for runtime " + value.getRuntimeName());
      getInjector(value.getRuntimeName()).getInstance(ResourceReleaseHandler.class).onNext(value);
    } catch (InjectionException e) {
      e.printStackTrace();
    }
  }

  public void onNext(final ResourceRequestEvent value) {
    try {
      System.err.println("Resource request called for runtime " + value.getRuntimeName());
      getInjector(value.getRuntimeName()).getInstance(ResourceRequestHandler.class).onNext(value);
    } catch (InjectionException e) {
      e.printStackTrace();
    }
  }
}
