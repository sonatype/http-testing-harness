/*
 * Copyright (c) 2010-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.tests.http.runner.junit;

import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.api.ServerProviderBuilder;

import com.google.common.base.Throwables;
import org.junit.rules.ExternalResource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Junit4 {@link ExternalResource} rule that manages {@link ServerProvider} lifecycle.
 */
public class ServerResource
    extends ExternalResource
{
  private final ServerProviderBuilder serverProviderBuilder;

  private ServerProvider serverProvider;

  public ServerResource(final ServerProvider serverProvider) {
    checkNotNull(serverProvider);
    this.serverProviderBuilder = new ServerProviderBuilder()
    {
      @Override
      public ServerProvider buildServerProvider() {
        return serverProvider;
      }
    };
  }

  public ServerResource(final ServerProviderBuilder serverProviderBuilder) {
    this.serverProviderBuilder = checkNotNull(serverProviderBuilder);
  }

  public ServerProvider getServerProvider() {
    return serverProvider;
  }

  protected void before() throws Throwable {
    serverProvider = serverProviderBuilder.buildServerProvider();
    serverProvider.start();
  }

  protected void after() {
    try {
      serverProvider.stop();
      serverProvider = null;
    }
    catch (Exception e) {
      Throwables.propagate(e);
    }
  }

}