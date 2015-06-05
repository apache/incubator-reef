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
package org.apache.reef.tang.util;

import java.util.HashMap;
import java.util.Map;

public class MonotonicHashMap<T, U> extends HashMap<T, U> {
  private static final long serialVersionUID = 1L;

  @Override
  public U put(T key, U value) {
    U old = super.get(key);
    if (old != null) {
      throw new IllegalArgumentException("Attempt to re-add: [" + key
          + "] old value: " + old + " new value " + value);
    }
    return super.put(key, value);
  }

  @Override
  public void putAll(Map<? extends T, ? extends U> m) {
    for (T t : m.keySet()) {
      if (containsKey(t)) {
        put(t, m.get(t)); // guaranteed to throw.
      }
    }
    for (T t : m.keySet()) {
      put(t, m.get(t));
    }
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public U remove(Object o) {
    throw new UnsupportedOperationException();
  }
}
