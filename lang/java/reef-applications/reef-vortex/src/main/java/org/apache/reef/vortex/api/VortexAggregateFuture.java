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
package org.apache.reef.vortex.api;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.reef.annotations.Unstable;
import org.apache.reef.annotations.audience.ClientSide;
import org.apache.reef.annotations.audience.Private;
import org.apache.reef.annotations.audience.Public;
import org.apache.reef.vortex.common.VortexFutureDelegate;
import org.apache.reef.wake.remote.Codec;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * The interface between user code and aggregation Tasklets.
 * Thread safety: This class is not meant to be used in a multi-threaded fashion.
 * TODO[JIRA REEF-1131]: Create and run tests once functional.
 */
@Public
@ClientSide
@NotThreadSafe
@Unstable
public final class VortexAggregateFuture<TInput, TOutput> implements VortexFutureDelegate {
  private static final Logger LOG = Logger.getLogger(VortexAggregateFuture.class.getName());

  private final Executor executor;
  private final Codec<TOutput> aggOutputCodec;
  private final BlockingQueue<AggregateResult> resultQueue;
  private final Map<Integer, TInput> taskletIdInputMap;
  private final FutureCallback<AggregateResult<TInput, TOutput>> callbackHandler;

  @Private
  public VortexAggregateFuture(final Executor executor,
                               final Map<Integer, TInput> taskletIdInputMap,
                               final Codec<TOutput> aggOutputCodec,
                               final FutureCallback<AggregateResult<TInput, TOutput>> callbackHandler) {
    this.executor = executor;
    this.taskletIdInputMap = new HashMap<>(taskletIdInputMap);
    this.resultQueue = new ArrayBlockingQueue<>(taskletIdInputMap.size());
    this.aggOutputCodec = aggOutputCodec;
    this.callbackHandler = callbackHandler;
  }

  /**
   * @return the next aggregation result for the future, null if no more results.
   */
  public synchronized AggregateResult get() throws InterruptedException {
    if (taskletIdInputMap.isEmpty()) {
      return null;
    }

    return resultQueue.take();
  }

  /**
   * @param timeout the timeout for the operation.
   * @param timeUnit the time unit of the timeout.
   * @return the next aggregation result for the future, within the user specified timeout, null if no more results.
   * @throws TimeoutException if time out hits.
   */
  public synchronized AggregateResult get(final long timeout,
                                          final TimeUnit timeUnit) throws InterruptedException, TimeoutException {
    if (taskletIdInputMap.isEmpty()) {
      return null;
    }

    final AggregateResult result = resultQueue.poll(timeout, timeUnit);

    if (result == null) {
      throw new TimeoutException();
    }

    return result;
  }

  /**
   * @return true if there are no more results to poll.
   */
  public synchronized boolean isDone() {
    return taskletIdInputMap.size() == 0;
  }

  /**
   * A Tasklet associated with the aggregation has completed.
   */
  @Private
  @Override
  public void completed(final int taskletId, final byte[] serializedResult) {
    try {
      // TODO[REEF-1113]: Handle serialization failure separately in Vortex
      final TOutput result = aggOutputCodec.decode(serializedResult);
      removeCompletedTasklets(result, Collections.singletonList(taskletId));
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Aggregation has completed for a list of Tasklets, with an aggregated result.
   */
  @Private
  @Override
  public void aggregationCompleted(final List<Integer> taskletIds, final byte[] serializedResult) {
    try {
      // TODO[REEF-1113]: Handle serialization failure separately in Vortex
      final TOutput result = aggOutputCodec.decode(serializedResult);
      removeCompletedTasklets(result, taskletIds);
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * A Tasklet associated with the aggregation has failed.
   */
  @Private
  @Override
  public void threwException(final int taskletId, final Exception exception) {
    try {
      removeFailedTasklets(exception, Collections.singletonList(taskletId));
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * A list of Tasklets has failed during aggregation phase.
   */
  @Private
  @Override
  public void aggregationThrewException(final List<Integer> taskletIds, final Exception exception) {
    try {
      removeFailedTasklets(exception, taskletIds);
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Not implemented for local aggregation.
   */
  @Private
  @Override
  public void cancelled(final int taskletId) {
    throw new NotImplementedException("Tasklet cancellation not supported in aggregations.");
  }

  /**
   * Removes completed Tasklets from Tasklets that are expected and invoke callback.
   */
  private synchronized void removeCompletedTasklets(final TOutput output, final List<Integer> taskletIds)
      throws InterruptedException {
    final AggregateResult result =
        new AggregateResult(output, getInputs(taskletIds), taskletIdInputMap.size() > 0);

    if (callbackHandler != null) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          callbackHandler.onSuccess(result);
        }
      });
    }

    resultQueue.put(result);
  }

  /**
   * Removes failed Tasklets from Tasklets that are expected and invokes callback.
   */
  private synchronized void removeFailedTasklets(final Exception exception, final List<Integer> taskletIds)
      throws InterruptedException {

    final List<TInput> inputs = getInputs(taskletIds);
    final AggregateResult failure =
        new AggregateResult(exception, inputs, taskletIdInputMap.size() > 0);

    if (callbackHandler != null) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          // TODO[JIRA REEF-1129]: Add documentation in VortexThreadPool.
          callbackHandler.onFailure(new VortexAggregateException(exception, inputs));
        }
      });
    }

    resultQueue.put(failure);
  }

  /**
   * Gets the inputs on Tasklet aggregation completion.
   */
  private synchronized List<TInput> getInputs(final List<Integer> taskletIds) {

    final List<TInput> inputList = new ArrayList<>(taskletIds.size());

    for(final int taskletId : taskletIds) {
      inputList.add(taskletIdInputMap.remove(taskletId));
    }

    return inputList;
  }
}