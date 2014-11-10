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

package org.apache.reef.util;

import org.apache.reef.tang.Injector;
import org.apache.reef.tang.Tang;
import org.apache.reef.tang.exceptions.InjectionException;
import org.apache.reef.util.logging.LogLevel;
import org.apache.reef.util.logging.LoggingScope;
import org.apache.reef.util.logging.LoggingScopeFactory;
import org.apache.reef.util.logging.LoggingScopeImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test LoggingScope
 */
public class LoggingScopeTest {

  private LoggingScopeFactory logFactory;

  @Before
  public void setUp() throws InjectionException {
    final Injector i = Tang.Factory.getTang().newInjector(Tang.Factory.getTang().newConfigurationBuilder().build());
    i.bindVolatileParameter(LogLevel.class, Level.INFO);
    logFactory = i.getInstance(LoggingScopeFactory.class);
  }

  /**
   * test getNewLoggingScope() in LoggingScopeFactory that injects  LoggingScope object
   *
   * @throws Exception
   */
  @Test
  public void testGetNewLoggingScope() throws InjectionException {
    try (final LoggingScope ls = logFactory.getNewLoggingScope("test")) {
       Assert.assertTrue(true);
    }
  }

  /**
   * Test creating ReefLoggingScope object directly
   * @throws Exception
   */
  @Test
  public void testNewLoggingScope() {
    try (final LoggingScope ls = new LoggingScopeImpl(Logger.getLogger(LoggingScopeFactory.class.getName()), Level.INFO, "test"))
    {
      Assert.assertTrue(true);
    }
  }

  /**
   *  Test creating ReefLoggingScope object with params
   * @throws Exception
   */
  @Test
  public void testNewLoggingScopeConstructorWithParameters() {
    try (final LoggingScope ls = new LoggingScopeImpl(Logger.getLogger(LoggingScopeFactory.class.getName()), Level.INFO, "test first string = {0}, second = {1}", new Object[] { "first", "second" }))
    {
      Assert.assertTrue(true);
    }
  }

  /**
   * Test calling predefined method in LoggingScopeFactory
   *
   * @throws Exception
   */
  @Test
  public void testLoggingScopeFactory() {
    try (final LoggingScope ls = logFactory.activeContextReceived("test"))
    {
      Assert.assertTrue(true);
    }
  }
}
