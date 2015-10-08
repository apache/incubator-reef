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
package org.apache.reef.io.network.group.impl.driver;

import org.apache.reef.driver.evaluator.FailedEvaluator;
import org.apache.reef.driver.task.FailedTask;
import org.apache.reef.driver.task.RunningTask;
import org.apache.reef.driver.task.TaskConfiguration;
import org.apache.reef.io.network.group.impl.GroupCommunicationMessage;
import org.apache.reef.io.network.group.impl.config.BroadcastOperatorSpec;
import org.apache.reef.io.network.group.impl.config.ReduceOperatorSpec;
import org.apache.reef.io.network.group.impl.utils.BroadcastingEventHandler;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.annotations.Name;
import org.apache.reef.tang.annotations.NamedParameter;
import org.apache.reef.tang.formats.AvroConfigurationSerializer;
import org.apache.reef.task.Task;
import org.apache.reef.wake.EStage;
import org.apache.reef.wake.EventHandler;
import org.apache.reef.wake.impl.ThreadPoolStage;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link CommunicationGroupDriverImpl}.
 */
public final class CommunicationGroupDriverImplTest {

  /**
   * Check that the topology builds up as expected even when the root task is added after child tasks start running.
   */
  @Test
  public void testLateRootTask() throws InterruptedException {
    final String rootTaskId = "rootTaskId";
    final String[] childTaskIds = new String[]{"childTaskId1", "childTaskId2", "childTaskId3"};
    final AtomicInteger numMsgs = new AtomicInteger(0);

    final EStage<GroupCommunicationMessage> senderStage =
        new ThreadPoolStage<>(new EventHandler<GroupCommunicationMessage>() {
            @Override
            public void onNext(final GroupCommunicationMessage msg) {
              numMsgs.getAndIncrement();
            }
        }, 1);

    final CommunicationGroupDriverImpl communicationGroupDriver = new CommunicationGroupDriverImpl(
        GroupName.class, new AvroConfigurationSerializer(), senderStage,
        new BroadcastingEventHandler<RunningTask>(), new BroadcastingEventHandler<FailedTask>(),
        new BroadcastingEventHandler<FailedEvaluator>(), new BroadcastingEventHandler<GroupCommunicationMessage>(),
        "DriverId", 4, 2);

    communicationGroupDriver
        .addBroadcast(BroadcastOperatorName.class,
            BroadcastOperatorSpec.newBuilder().setSenderId(rootTaskId).build())
        .addReduce(ReduceOperatorName.class,
            ReduceOperatorSpec.newBuilder().setReceiverId(rootTaskId).build());

    final ExecutorService pool = Executors.newFixedThreadPool(4);
    final CountDownLatch countDownLatch = new CountDownLatch(4);

    // first add child tasks and start them up
    for (int index = 0; index < 3; index++) {
      final String childId = childTaskIds[index];
      pool.submit(new Runnable() {
        @Override
        public void run() {
          final Configuration childTaskConf = TaskConfiguration.CONF
              .set(TaskConfiguration.IDENTIFIER, childId)
              .set(TaskConfiguration.TASK, DummyTask.class)
              .build();
          communicationGroupDriver.addTask(childTaskConf);
          communicationGroupDriver.runTask(childId);
          countDownLatch.countDown();
        }
      });
    }

    // next add the root task
    pool.submit(new Runnable() {
      @Override
      public void run() {
        try {
          // purposely delay the addition of the root task
          Thread.sleep(3000);
        } catch (final InterruptedException e) {
          throw new RuntimeException(e);
        }
        final Configuration rootTaskConf = TaskConfiguration.CONF
            .set(TaskConfiguration.IDENTIFIER, rootTaskId)
            .set(TaskConfiguration.TASK, DummyTask.class)
            .build();

        communicationGroupDriver.addTask(rootTaskConf);
        communicationGroupDriver.runTask(rootTaskId);
        countDownLatch.countDown();
      }
    });

    pool.shutdown();
    countDownLatch.await(5000, TimeUnit.MILLISECONDS);

    // 3 connections: rootTask - childTask1, rootTask - childTask2, childTask1 - childTask-3
    // 2 messages per connection
    // 2 operations (broadcast & reduce)
    // gives us a total of 3*2*2 = 12 messages
    assertEquals("number of messages sent from driver", 12, numMsgs.get());

  }

  private final class DummyTask implements Task {
    @Override
    public byte[] call(final byte[] memento) throws Exception {
      return null;
    }
  }

  @NamedParameter()
  private final class GroupName implements Name<String> {
  }

  @NamedParameter()
  private final class BroadcastOperatorName implements Name<String> {
  }

  @NamedParameter()
  private final class ReduceOperatorName implements Name<String> {
  }
}

