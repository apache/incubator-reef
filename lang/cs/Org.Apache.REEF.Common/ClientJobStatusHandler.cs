﻿/**
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

using Org.Apache.REEF.Common.Client;
using Org.Apache.REEF.Common.ProtoBuf.ClienRuntimeProto;
using Org.Apache.REEF.Common.ProtoBuf.ReefProtocol;
using Org.Apache.REEF.Common.ProtoBuf.ReefServiceProto;
using Org.Apache.REEF.Utilities;
using Org.Apache.REEF.Utilities.Logging;
using Org.Apache.REEF.Wake.Remote;
using Org.Apache.REEF.Wake.Time;
using System;

namespace Org.Apache.REEF.Common
{
    public class ClientJobStatusHandler : IJobMessageObserver, IObserver<StartTime>
    {
        private static readonly Logger LOGGER = Logger.GetLogger(typeof(ClientJobStatusHandler));

        private IClock _clock;

        private string _jobId;

        private IObserver<JobStatusProto> _jobStatusHandler;

        private IDisposable _jobControlChannel;

        State _state = State.INIT;

        public ClientJobStatusHandler(
            IRemoteManager<IRemoteMessage<REEFMessage>> remoteManager,
            IClock clock,
            IObserver<JobControlProto> jobControlHandler,
            string jobId,
            string clientRID)
        {
            _clock = clock;
            _jobId = jobId;
            _jobStatusHandler = null;
            _jobControlChannel = null;
            //_jobStatusHandler = remoteManager.GetRemoteObserver()
            //_jobControlChannel = remoteManager.RegisterObserver()
        }

        public void Dispose(Optional<Exception> e)
        {
            try
            {
                if (e.IsPresent())
                {
                    OnError(e.Value);
                }
                else
                {
                    JobStatusProto proto = new JobStatusProto();
                    proto.identifier = _jobId;
                    proto.state = State.DONE;
                    Send(proto);
                }
            }
            catch (Exception ex)
            {
                Org.Apache.REEF.Utilities.Diagnostics.Exceptions.CaughtAndThrow(ex, Level.Warning, "Error closing ClientJobStatusHandler", LOGGER);
            }

            try
            {
                _jobControlChannel.Dispose();
            }
            catch (Exception ex)
            {
                Org.Apache.REEF.Utilities.Diagnostics.Exceptions.CaughtAndThrow(ex, Level.Warning, "Error closing jobControlChannel", LOGGER);
            }
        }

        public void OnNext(byte[] value)
        {
            LOGGER.Log(Level.Info, "Job message from {0}" + _jobId);   
            SendInit();
            JobStatusProto proto = new JobStatusProto();
            proto.identifier = _jobId;
            proto.state = State.RUNNING;
            proto.message = value;
            Send(proto);
        }

        public void OnNext(StartTime value)
        {
            LOGGER.Log(Level.Info, "StartTime:" + value);
            SendInit();
        }

        public void OnError(Exception error)
        {
            LOGGER.Log(Level.Error, "job excemption", error);
            JobStatusProto proto = new JobStatusProto();
            proto.identifier = _jobId;
            proto.state = State.FAILED;
            proto.exception = ByteUtilities.StringToByteArrays(error.Message);
            _clock.Dispose();
        }

        public void OnCompleted()
        {
            throw new NotImplementedException();
        }

        private void Send(JobStatusProto status)
        {
            LOGGER.Log(Level.Info, "Sending job status " + status);
            _jobStatusHandler.OnNext(status);
        }

        private void SendInit()
        {
            if (_state == State.INIT)
            {
                JobStatusProto proto = new JobStatusProto();
                proto.identifier = _jobId;
                proto.state = State.INIT;
                Send(proto);
                _state = State.RUNNING;
            }
        }
    }
}
