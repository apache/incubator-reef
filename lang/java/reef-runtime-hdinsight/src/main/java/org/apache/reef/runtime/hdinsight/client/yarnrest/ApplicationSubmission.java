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
package org.apache.reef.runtime.hdinsight.client.yarnrest;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an ApplicationSubmission to the YARN REST API.
 */
public final class ApplicationSubmission {

  public static final String DEFAULT_QUEUE = "default";
  private String queue = DEFAULT_QUEUE;

  public static final int DEFAULT_PRIORITY = 3;
  private int priority = DEFAULT_PRIORITY;

  public static final int DEFAULT_MAX_APP_ATTEMPTS = 1;
  private int maxAppAttempts = DEFAULT_MAX_APP_ATTEMPTS;

  public static final String DEFAULT_APPLICATION_TYPE = "YARN";
  private String applicationType = DEFAULT_APPLICATION_TYPE;

  public static final boolean DEFAULT_KEEP_CONTAINERS_ACROSS_APPLICATION_ATTEMPTS = false;
  private boolean keepContainers = DEFAULT_KEEP_CONTAINERS_ACROSS_APPLICATION_ATTEMPTS;

  public static final boolean DEFAULT_UNMANAGED_AM = false;
  private boolean isUnmanagedAM = DEFAULT_UNMANAGED_AM;

  private String applicationId;
  private String applicationName;
  private AmContainerSpec amContainerSpec;
  private Resource resource;
  private List<String> applicationTags = new ArrayList<>();

  @JsonProperty(Constants.APPLICATION_ID)
  public String getApplicationId() {
    return applicationId;
  }

  public ApplicationSubmission setApplicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  @JsonProperty(Constants.APPLICATION_NAME)
  public String getApplicationName() {
    return applicationName;
  }

  public ApplicationSubmission setApplicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  @JsonProperty(Constants.APPLICATION_TYPE)
  public String getApplicationType() {
    return applicationType;
  }

  public ApplicationSubmission setApplicationType(String applicationType) {
    this.applicationType = applicationType;
    return this;
  }

  @JsonProperty(Constants.AM_CONTAINER_SPEC)
  public AmContainerSpec getAmContainerSpec() {
    return amContainerSpec;
  }

  public ApplicationSubmission setAmContainerSpec(AmContainerSpec amContainerSpec) {
    this.amContainerSpec = amContainerSpec;
    return this;
  }

  @JsonProperty(Constants.UNMANAGED_AM)
  public boolean isUnmanagedAM() {
    return isUnmanagedAM;
  }

  public ApplicationSubmission setUnmanagedAM(boolean isUnmanagedAM) {
    this.isUnmanagedAM = isUnmanagedAM;
    return this;
  }

  @JsonProperty(Constants.KEEP_CONTAINERS_ACROSS_APPLICATION_ATTEMPTS)
  public boolean isKeepContainers() {
    return keepContainers;
  }

  public ApplicationSubmission setKeepContainers(boolean keepContainers) {
    this.keepContainers = keepContainers;
    return this;
  }

  @JsonProperty(Constants.MAX_APP_ATTEMPTS)
  public int getMaxAppAttempts() {
    return maxAppAttempts;
  }

  public ApplicationSubmission setMaxAppAttempts(int maxAppAttempts) {
    this.maxAppAttempts = maxAppAttempts;
    return this;
  }

  @JsonProperty(Constants.PRIORITY)
  public int getPriority() {
    return priority;
  }

  public ApplicationSubmission setPriority(int priority) {
    this.priority = priority;
    return this;
  }

  @JsonProperty(Constants.QUEUE)
  public String getQueue() {
    return queue;
  }

  public ApplicationSubmission setQueue(String queue) {
    this.queue = queue;
    return this;
  }

  @JsonProperty(Constants.RESOURCE)
  public Resource getResource() {
    return resource;
  }

  public ApplicationSubmission setResource(Resource resource) {
    this.resource = resource;
    return this;
  }

  public ApplicationSubmission addApplicationTag(String tag) {
    this.applicationTags.add(tag);
    return this;
  }

  @JsonProperty(Constants.APPLICATION_TAGS)
  public List<String> getApplicationTags() {
    return this.applicationTags;
  }

  public ApplicationSubmission setApplicationTags(final List<String> applicationTags) {
    this.applicationTags = applicationTags;
    return this;
  }

  @Override
  public String toString() {
    return Constants.APPLICATION_SUBMISSION + " {" +
          ", " + Constants.APPLICATION_ID + "='" + applicationId + '\'' +
          ", " + Constants.APPLICATION_NAME + "='" + applicationName + '\'' +
          ", " + Constants.QUEUE + "='" + queue + '\'' +
          ", " + Constants.PRIORITY + "=" + priority +
          ", " + Constants.AM_CONTAINER_SPEC + "=" + amContainerSpec +
          ", " + Constants.UNMANAGED_AM + "=" + isUnmanagedAM +
          ", " + Constants.MAX_APP_ATTEMPTS + "=" + maxAppAttempts +
          ", " + Constants.RESOURCE + "=" + resource +
          ", " + Constants.APPLICATION_TYPE + "='" + applicationType + '\'' +
          ", " + Constants.KEEP_CONTAINERS_ACROSS_APPLICATION_ATTEMPTS + "=" + keepContainers +
          ", " + Constants.APPLICATION_TAGS + "=" + applicationTags +
          '}';
  }
}
