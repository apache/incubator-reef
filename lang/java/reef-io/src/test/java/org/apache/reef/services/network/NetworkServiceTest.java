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
package org.apache.reef.services.network;

import org.apache.reef.exception.evaluator.NetworkException;
import org.apache.reef.io.network.Connection;
import org.apache.reef.io.network.ConnectionFactory;
import org.apache.reef.io.network.Message;
import org.apache.reef.io.network.NetworkService;
import org.apache.reef.io.network.impl.NetworkServiceConfiguration;
import org.apache.reef.io.network.impl.NetworkServiceParameters;
import org.apache.reef.io.network.naming.NameClientConfiguration;
import org.apache.reef.io.network.naming.NameServer;
import org.apache.reef.io.network.util.StringCodec;
import org.apache.reef.io.network.util.StringIdentifierFactory;
import org.apache.reef.services.network.util.Monitor;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Configurations;
import org.apache.reef.tang.Injector;
import org.apache.reef.tang.Tang;
import org.apache.reef.tang.annotations.Name;
import org.apache.reef.tang.exceptions.InjectionException;
import org.apache.reef.wake.EventHandler;
import org.apache.reef.wake.Identifier;
import org.apache.reef.wake.IdentifierFactory;
import org.apache.reef.wake.remote.address.LocalAddressProvider;
import org.apache.reef.wake.remote.address.LocalAddressProviderFactory;
import org.apache.reef.wake.remote.impl.ObjectSerializableCodec;
import org.apache.reef.wake.remote.transport.LinkListener;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.net.SocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Network service test
 */
public class NetworkServiceTest {
  private static final Logger LOG = Logger.getLogger(NetworkServiceTest.class.getName());

  private final LocalAddressProvider localAddressProvider;
  private final String localAddress;

  public NetworkServiceTest() throws InjectionException {
    localAddressProvider = LocalAddressProviderFactory.getInstance();
    localAddress = localAddressProvider.getLocalAddress();
  }

  @Rule
  public TestName name = new TestName();

  public static final class GroupCommClientId implements Name<String> {
  }

  public static final class ShuffleClientId implements Name<String> {

  }

  /**
   * NetworkService messaging test
   */
  @Test
  public void testMessagingNetworkService() throws Exception {
    LOG.log(Level.FINEST, name.getMethodName());

    // name server
    Injector injector = Tang.Factory.getTang().newInjector();
    NameServer nameServer = injector.getInstance(NameServer.class);
    final Configuration netConf = Configurations.merge(NameClientConfiguration.CONF
        .set(NameClientConfiguration.NAME_SERVER_HOSTNAME, localAddress)
        .set(NameClientConfiguration.NAME_SERVICE_PORT, nameServer.getPort())
        .build(), NetworkServiceConfiguration.getServiceConfiguration());

    final int numMessages = 2000;
    final Monitor monitor = new Monitor();

    LOG.log(Level.FINEST, "=== Test network service receiver start");
    // network service for task2
    Injector ij2 = injector.forkInjector(netConf);
    final NetworkService ns2 = ij2.getInstance(NetworkService.class);
    IdentifierFactory factory = ij2.getNamedInstance(NetworkServiceParameters.IdentifierFactory.class);
    ns2.registerId(factory.getNewInstance("task2"));
    ns2.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, numMessages), new TestListener<String>());

    // connection for receiving messages
    ConnectionFactory<String> receiver = ns2.getConnectionFactory(GroupCommClientId.class);

    // network service for task1
    LOG.log(Level.FINEST, "=== Test network service sender start");
    Injector ij3 = injector.forkInjector(netConf);
    final NetworkService ns1 = ij3.getInstance(NetworkService.class);
    ns1.registerId(factory.getNewInstance("task1"));
    ns1.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, numMessages), new TestListener<String>());
    // connection for sending messages
    ConnectionFactory<String> sender = ns1.getConnectionFactory(GroupCommClientId.class);

    final Identifier destId = factory.getNewInstance("task2");
    final org.apache.reef.io.network.Connection<String> conn = sender.newConnection(destId);
    try {
      conn.open();
      for (int count = 0; count < numMessages; ++count) {
        conn.write("hello! " + count);
      }
      monitor.mwait();

    } catch (NetworkException e) {
      e.printStackTrace();
    }
    conn.close();

    ns1.close();
    ns2.close();

    nameServer.close();
  }

  /**
   * Multiple connection factories test
   */
  @Test
  public void testMultipleConnectionFactoriesTest() throws Exception {
    LOG.log(Level.FINEST, name.getMethodName());

    ExecutorService executor = Executors.newFixedThreadPool(5);

    // name server
    Injector injector = Tang.Factory.getTang().newInjector();
    NameServer nameServer = injector.getInstance(NameServer.class);
    final Configuration netConf = Configurations.merge(NameClientConfiguration.CONF
        .set(NameClientConfiguration.NAME_SERVER_HOSTNAME, localAddress)
        .set(NameClientConfiguration.NAME_SERVICE_PORT, nameServer.getPort())
        .build(), NetworkServiceConfiguration.getServiceConfiguration());

    final int groupcommMessages = 1000;
    final int shuffleMessges = 2000;
    final Monitor monitor = new Monitor();
    final Monitor monitor2 = new Monitor();

    LOG.log(Level.FINEST, "=== Test network service receiver start");
    // network service for task2
    Injector ij2 = injector.forkInjector(netConf);
    final NetworkService ns2 = ij2.getInstance(NetworkService.class);
    IdentifierFactory factory = ij2.getNamedInstance(NetworkServiceParameters.IdentifierFactory.class);
    ns2.registerId(factory.getNewInstance("task2"));
    ns2.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, groupcommMessages), new TestListener<String>());
    ns2.registerConnectionFactory(ShuffleClientId.class, new ObjectSerializableCodec<Integer>(), new MessageHandler<Integer>(monitor2, shuffleMessges), new TestListener<Integer>());

    // connection for receiving messages
    ConnectionFactory<String> gcReceiver = ns2.getConnectionFactory(GroupCommClientId.class);
    ConnectionFactory<Integer> shuffleReceiver = ns2.getConnectionFactory(ShuffleClientId.class);

    // network service for task1
    LOG.log(Level.FINEST, "=== Test network service sender start");
    Injector ij3 = injector.forkInjector(netConf);
    final NetworkService ns1 = ij3.getInstance(NetworkService.class);
    ns1.registerId(factory.getNewInstance("task1"));
    ns1.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, groupcommMessages), new TestListener<String>());
    ns1.registerConnectionFactory(ShuffleClientId.class, new ObjectSerializableCodec<Integer>(), new MessageHandler<Integer>(monitor2, shuffleMessges), new TestListener<Integer>());

    // connection for sending messages
    ConnectionFactory<String> gcSender = ns1.getConnectionFactory(GroupCommClientId.class);
    ConnectionFactory<Integer> shuffleSender = ns1.getConnectionFactory(ShuffleClientId.class);

    final Identifier destId = factory.getNewInstance("task2");
    final Connection<String> conn = gcSender.newConnection(destId);
    final Connection<Integer> conn2 = shuffleSender.newConnection(destId);
    try {
      conn.open();
      conn2.open();

      executor.submit(new Runnable() {
        @Override
        public void run() {
          for (int count = 0; count < groupcommMessages; ++count) {
            try {
              conn.write("hello! " + count);
            } catch (NetworkException e) {
              throw new RuntimeException(e);
            }
          }
        }
      });

      executor.submit(new Runnable() {
        @Override
        public void run() {
          for (int count = 0; count < shuffleMessges; ++count) {
            try {
              conn2.write(count);
            } catch (NetworkException e) {
              throw new RuntimeException(e);
            }
          }
        }
      });

      monitor.mwait();
      monitor2.mwait();

    } catch (NetworkException e) {
      e.printStackTrace();
    }
    conn.close();

    ns1.close();
    ns2.close();

    executor.shutdown();
    nameServer.close();
  }

  /**
   * NetworkService messaging rate benchmark
   */
  @Test
  public void testMessagingNetworkServiceRate() throws Exception {
    LOG.log(Level.FINEST, name.getMethodName());

    // name server
    Injector injector = Tang.Factory.getTang().newInjector();
    NameServer server = injector.getInstance(NameServer.class);
    final Configuration netConf = Configurations.merge(NameClientConfiguration.CONF
        .set(NameClientConfiguration.NAME_SERVER_HOSTNAME, localAddress)
        .set(NameClientConfiguration.NAME_SERVICE_PORT, server.getPort())
        .build(), NetworkServiceConfiguration.getServiceConfiguration());

    final int[] messageSizes = {1, 16, 32, 64, 512, 64 * 1024, 1024 * 1024};

    for (int size : messageSizes) {
      final IdentifierFactory factory = new StringIdentifierFactory();

      final int numMessages = 300000 / (Math.max(1, size / 512));
      final Monitor monitor = new Monitor();
      injector = Tang.Factory.getTang().newInjector();

      // network service
      final String name2 = "receiver";
      final String name1 = "sender";
      LOG.log(Level.FINEST, "=== Test network service receiver start");
      // network service for task2
      Injector ij2 = injector.forkInjector(netConf);
      final NetworkService ns2 = ij2.getInstance(NetworkService.class);
      ns2.registerId(factory.getNewInstance(name2));
      ns2.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, numMessages), new TestListener<String>());

      LOG.log(Level.FINEST, "=== Test network service sender start");
      Injector ij3 = injector.forkInjector(netConf);
      final NetworkService ns1 = ij3.getInstance(NetworkService.class);
      ns1.registerId(factory.getNewInstance(name1));
      ns1.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, numMessages), new TestListener<String>());
      // connection for sending messages
      ConnectionFactory<String> sender = ns1.getConnectionFactory(GroupCommClientId.class);
      Identifier destId = factory.getNewInstance(name2);
      Connection<String> conn = sender.newConnection(destId);

      // build the message
      StringBuilder msb = new StringBuilder();
      for (int i = 0; i < size; i++) {
        msb.append("1");
      }
      String message = msb.toString();

      long start = System.currentTimeMillis();
      try {
        for (int i = 0; i < numMessages; i++) {
          conn.open();
          conn.write(message);
        }
        monitor.mwait();
      } catch (NetworkException e) {
        e.printStackTrace();
      }
      long end = System.currentTimeMillis();
      double runtime = ((double) end - start) / 1000;
      LOG.log(Level.INFO, "size: " + size + "; messages/s: " + numMessages / runtime + " bandwidth(bytes/s): " + ((double) numMessages * 2 * size) / runtime);// x2 for unicode chars
      conn.close();

      ns1.close();
      ns2.close();

    }
    server.close();

  }

  /**
   * NetworkService messaging rate benchmark
   */
  @Test
  public void testMessagingNetworkServiceRateDisjoint() throws Exception {
    LOG.log(Level.FINEST, name.getMethodName());

    final IdentifierFactory factory = new StringIdentifierFactory();

    // name server
    Injector injector = Tang.Factory.getTang().newInjector();
    NameServer server = injector.getInstance(NameServer.class);
    final Configuration netConf = Configurations.merge(NameClientConfiguration.CONF
        .set(NameClientConfiguration.NAME_SERVER_HOSTNAME, localAddress)
        .set(NameClientConfiguration.NAME_SERVICE_PORT, server.getPort())
        .build(), NetworkServiceConfiguration.getServiceConfiguration());

    BlockingQueue<Object> barrier = new LinkedBlockingQueue<Object>();

    int numThreads = 4;
    final int size = 2000;
    final int numMessages = 300000 / (Math.max(1, size / 512));
    final int totalNumMessages = numMessages * numThreads;


    ExecutorService e = Executors.newCachedThreadPool();
    for (int t = 0; t < numThreads; t++) {
      final int tt = t;

      e.submit(new Runnable() {
        public void run() {
          try {
            Monitor monitor = new Monitor();
            Injector injector = Tang.Factory.getTang().newInjector(netConf);
            // network service
            final String name2 = "task2-" + tt;
            final String name1 = "task1-" + tt;


            LOG.log(Level.FINEST, "=== Test network service receiver start");
            final NetworkService ns2 = injector.getInstance(NetworkService.class);
            ns2.registerId(factory.getNewInstance(name2));
            ns2.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, numMessages), new TestListener<String>());
            ConnectionFactory<String> receiver = ns2.getConnectionFactory(GroupCommClientId.class);

            LOG.log(Level.FINEST, "=== Test network service sender start");
            Injector ij1 = Tang.Factory.getTang().newInjector(netConf);
            final NetworkService ns1 = ij1.getInstance(NetworkService.class);
            ns1.registerId(factory.getNewInstance(name1));
            ns1.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, numMessages), new TestListener<String>());
            ConnectionFactory<String> sender = ns1.getConnectionFactory(GroupCommClientId.class);
            Identifier destId = factory.getNewInstance(name2);
            Connection<String> conn = sender.newConnection(destId);

            // build the message
            StringBuilder msb = new StringBuilder();
            for (int i = 0; i < size; i++) {
              msb.append("1");
            }
            String message = msb.toString();


            try {
              for (int i = 0; i < numMessages; i++) {
                conn.open();
                conn.write(message);
              }
              monitor.mwait();
            } catch (NetworkException e) {
              e.printStackTrace();
            }
            conn.close();

            ns1.close();
            ns2.close();
          } catch (Exception e) {
            e.printStackTrace();

          }
        }
      });
    }

    // start and time
    long start = System.currentTimeMillis();
    Object ignore = new Object();
    for (int i = 0; i < numThreads; i++) barrier.add(ignore);
    e.shutdown();
    e.awaitTermination(100, TimeUnit.SECONDS);
    long end = System.currentTimeMillis();

    double runtime = ((double) end - start) / 1000;
    LOG.log(Level.INFO, "size: " + size + "; messages/s: " + totalNumMessages / runtime + " bandwidth(bytes/s): " + ((double) totalNumMessages * 2 * size) / runtime);// x2 for unicode chars

    server.close();
  }

  @Test
  public void testMultithreadedSharedConnMessagingNetworkServiceRate() throws Exception {
    LOG.log(Level.FINEST, name.getMethodName());

    final IdentifierFactory factory = new StringIdentifierFactory();

    // name server
    Injector injector = Tang.Factory.getTang().newInjector();
    NameServer server = injector.getInstance(NameServer.class);
    final Configuration netConf = Configurations.merge(NameClientConfiguration.CONF
        .set(NameClientConfiguration.NAME_SERVER_HOSTNAME, localAddress)
        .set(NameClientConfiguration.NAME_SERVICE_PORT, server.getPort())
        .build(), NetworkServiceConfiguration.getServiceConfiguration());

    final int[] messageSizes = {2000};// {1,16,32,64,512,64*1024,1024*1024};

    for (int size : messageSizes) {
      final int numMessages = 300000 / (Math.max(1, size / 512));
      int numThreads = 2;
      int totalNumMessages = numMessages * numThreads;
      final Monitor monitor = new Monitor();
      injector = Tang.Factory.getTang().newInjector(netConf);

      final String name2 = "receiver";
      final String name1 = "sender";


      LOG.log(Level.FINEST, "=== Test network service receiver start");
      NetworkService ns2 = injector.getInstance(NetworkService.class);
      ns2.registerId(factory.getNewInstance(name2));
      ns2.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, totalNumMessages), new TestListener<String>());

      LOG.log(Level.FINEST, "=== Test network service sender start");
      Injector ij1 = Tang.Factory.getTang().newInjector(netConf);
      NetworkService ns1 = ij1.getInstance(NetworkService.class);
      ns1.registerId(factory.getNewInstance(name1));
      ns1.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, totalNumMessages), new TestListener<String>());

      Identifier destId = factory.getNewInstance(name2);
      ConnectionFactory<String> connFactory = ns1.getConnectionFactory(GroupCommClientId.class);
      final Connection<String> conn = connFactory.newConnection(destId);
      conn.open();

      // build the message
      StringBuilder msb = new StringBuilder();
      for (int i = 0; i < size; i++) {
        msb.append("1");
      }
      final String message = msb.toString();

      ExecutorService e = Executors.newCachedThreadPool();

      long start = System.currentTimeMillis();
      for (int i = 0; i < numThreads; i++) {
        e.submit(new Runnable() {

          @Override
          public void run() {
            try {
              for (int i = 0; i < numMessages; i++) {
                conn.write(message);
              }
            } catch (NetworkException e) {
              e.printStackTrace();
            }
          }
        });
      }


      e.shutdown();
      e.awaitTermination(30, TimeUnit.SECONDS);
      monitor.mwait();

      long end = System.currentTimeMillis();
      double runtime = ((double) end - start) / 1000;

      LOG.log(Level.INFO, "size: " + size + "; messages/s: " + totalNumMessages / runtime + " bandwidth(bytes/s): " + ((double) totalNumMessages * 2 * size) / runtime);// x2 for unicode chars
      conn.close();

      ns1.close();
      ns2.close();
    }

    server.close();
  }

  /**
   * NetworkService messaging rate benchmark
   */
  @Test
  public void testMessagingNetworkServiceBatchingRate() throws Exception {
    LOG.log(Level.FINEST, name.getMethodName());

    IdentifierFactory factory = new StringIdentifierFactory();

    // name server
    Injector injector = Tang.Factory.getTang().newInjector();
    NameServer server = injector.getInstance(NameServer.class);
    final Configuration netConf = Configurations.merge(NameClientConfiguration.CONF
        .set(NameClientConfiguration.NAME_SERVER_HOSTNAME, localAddress)
        .set(NameClientConfiguration.NAME_SERVICE_PORT, server.getPort())
        .build(), NetworkServiceConfiguration.getServiceConfiguration());

    final int batchSize = 1024 * 1024;
    final int[] messageSizes = {32, 64, 512};

    for (int size : messageSizes) {
      final int numMessages = 300 / (Math.max(1, size / 512));
      final Monitor monitor = new Monitor();

      injector = Tang.Factory.getTang().newInjector();

      final String name2 = "receiver";
      final String name1 = "sender";

      LOG.log(Level.FINEST, "=== Test network service receiver start");
      Injector ij2 = injector.forkInjector(netConf);
      NetworkService ns2 = ij2.getInstance(NetworkService.class);
      ns2.registerId(factory.getNewInstance(name2));
      ns2.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, numMessages), new TestListener<String>());

      LOG.log(Level.FINEST, "=== Test network service sender start");
      Injector ij1 = injector.forkInjector(netConf);
      NetworkService ns1 = ij1.getInstance(NetworkService.class);
      ns1.registerId(factory.getNewInstance(name1));
      ns1.registerConnectionFactory(GroupCommClientId.class, new StringCodec(), new MessageHandler<String>(monitor, numMessages), new TestListener<String>());

      Identifier destId = factory.getNewInstance(name2);
      ConnectionFactory<String> connFactory = ns1.getConnectionFactory(GroupCommClientId.class);
      final Connection<String> conn = connFactory.newConnection(destId);
      conn.open();
      // build the message
      StringBuilder msb = new StringBuilder();
      for (int i = 0; i < size; i++) {
        msb.append("1");
      }
      String message = msb.toString();

      long start = System.currentTimeMillis();
      try {
        for (int i = 0; i < numMessages; i++) {
          StringBuilder sb = new StringBuilder();
          for (int j = 0; j < batchSize / size; j++) {
            sb.append(message);
          }
          conn.open();
          conn.write(sb.toString());
        }
        monitor.mwait();
      } catch (NetworkException e) {
        e.printStackTrace();
      }
      long end = System.currentTimeMillis();
      double runtime = ((double) end - start) / 1000;
      long numAppMessages = numMessages * batchSize / size;
      LOG.log(Level.INFO, "size: " + size + "; messages/s: " + numAppMessages / runtime + " bandwidth(bytes/s): " + ((double) numAppMessages * 2 * size) / runtime);// x2 for unicode chars
      conn.close();

      ns1.close();
      ns2.close();
    }

    server.close();
  }

  public final class MessageHandler<T> implements EventHandler<Message<T>> {

    private final int expected;
    private final Monitor monitor;
    private AtomicInteger count = new AtomicInteger(0);

    public MessageHandler(Monitor monitor,
                          Integer expected) {
      this.monitor = monitor;
      this.expected = expected;
    }

    @Override
    public void onNext(Message<T> value) {
      count.incrementAndGet();
      LOG.log(Level.FINE,
          "OUT: {0} received {1} from {2} to {3}",
          new Object[]{value, value.getSrcId(), value.getDestId()});

      for (final T obj : value.getData()) {
        LOG.log(Level.FINE, "OUT: data: {0}", obj);
      }

      if (count.get() == expected) {
        monitor.mnotify();
      }
    }
  }

  public final class TestListener<T> implements LinkListener<Message<T>> {

    @Override
    public void onSuccess(Message<T> message) {

    }

    @Override
    public void onException(Throwable cause, SocketAddress remoteAddress, Message<T> message) {

    }
  }
}
