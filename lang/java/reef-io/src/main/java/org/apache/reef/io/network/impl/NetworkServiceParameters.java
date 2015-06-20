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

import org.apache.reef.io.network.naming.NameClient;
import org.apache.reef.io.network.util.StringIdentifierFactory;
import org.apache.reef.tang.annotations.Name;
import org.apache.reef.tang.annotations.NamedParameter;

/**
 *
 */
public final class NetworkServiceParameters {

  /**
   * NameClient
   */
  @NamedParameter(doc = "NameClient")
  public static final class NameClientImpl implements Name<NameClient> {

  }

  /**
   * Port number of NetworkService
   */
  @NamedParameter(doc = "port number of NetworkService", default_value = "0")
  public static final class Port implements Name<Integer> {

  }

  /**
   * Identifier Factory of NetworkService
   */
  @NamedParameter(doc = "identifier factory of NetworkService", default_class = StringIdentifierFactory.class)
  public static final class IdentifierFactory implements Name<org.apache.reef.wake.IdentifierFactory> {

  }
}
