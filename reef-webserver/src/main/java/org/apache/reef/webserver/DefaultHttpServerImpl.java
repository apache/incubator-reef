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

package org.apache.reef.webserver;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DefaultHttpServerImpl. It provides an empty implementation for HttpServer. When it is used, no HttpServer is created and started.
 */
public final class DefaultHttpServerImpl implements HttpServer {
  /**
   * Standard Java logger.
   */
  private static final Logger LOG = Logger.getLogger(DefaultHttpServerImpl.class.getName());

  @Inject
  DefaultHttpServerImpl() {
    LOG.log(Level.INFO, "DefaultHttpServerImpl is used. No name Http Server is registered");
  }

  /**
   * It will be called from RuntimeStartHandler. No Jetty server in this case therefore do nothing.
   *
   * @throws Exception
   */
  @Override
  public void start() throws Exception {
  }

  /**
   * It will be called from RuntimeStopHandler Do nothing as no server is started.
   *
   * @throws Exception
   */
  @Override
  public void stop() throws Exception {
  }

  @Override
  public int getPort() {
    return 0;
  }

  /**
   * Implement the interface only but do nothing.
   *
   * @param httpHandler
   */
  @Override
  public void addHttpHandler(final HttpHandler httpHandler) {
  }
}