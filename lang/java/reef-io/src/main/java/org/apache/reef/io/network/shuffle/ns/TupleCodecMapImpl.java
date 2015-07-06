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
package org.apache.reef.io.network.shuffle.ns;

import org.apache.reef.io.network.shuffle.task.ShuffleTopologyClient;
import org.apache.reef.io.network.shuffle.task.Tuple;
import org.apache.reef.io.network.shuffle.topology.GroupingDescription;
import org.apache.reef.wake.remote.Codec;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
final class TupleCodecMapImpl implements TupleCodecMap {

  private final Map<String, Map<String, Codec<Tuple>>> tupleCodecMap;

  @Inject
  public TupleCodecMapImpl() {
    this.tupleCodecMap = new HashMap<>();
  }

  @Override
  public Codec<Tuple> getTupleCodec(final String topologyName, final String groupingName) {
    return tupleCodecMap.get(topologyName).get(groupingName);
  }

  @Override
  public void registerTupleCodecs(final ShuffleTopologyClient client) {
    final Map<String, Codec<Tuple>> tupleCodecMapForClient = new HashMap<>();
    for (final GroupingDescription description : client.getGroupingDescriptionMap().values()) {
      tupleCodecMapForClient.put(description.getGroupingName(), client.getTupleCodec(description.getGroupingName()));
    }

    tupleCodecMap.put(client.getTopologyName().getName(), tupleCodecMapForClient);
  }
}
