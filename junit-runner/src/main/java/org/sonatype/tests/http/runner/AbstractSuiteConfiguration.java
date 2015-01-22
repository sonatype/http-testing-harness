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
package org.sonatype.tests.http.runner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.sonatype.tests.http.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
public class AbstractSuiteConfiguration
    implements SuiteConfiguration
{

  private SuiteConfigurator configurator;

  private ThreadLocal<ServerProvider> provider = new ThreadLocal<ServerProvider>();

  public void setConfigurator(SuiteConfigurator configurator)
  {
    this.configurator = configurator;
  }

  public void before()
      throws Exception
  {
    provider.set(configurator().provider());
    if (provider.get() == null) {
      throw new IllegalArgumentException("Configurator failed, provider is null.");
    }
    configureProvider(provider.get());
    provider.get().start();
  }

  public void configureProvider(ServerProvider provider)
  {
  }

  public void after()
      throws Exception
  {
    if (provider.get() != null) {
      provider.get().stop();
    }
  }

  public String url()
  {
    return provider.get().getUrl().toExternalForm();
  }

  public ServerProvider provider()
  {
    return provider.get();
  }

  public String url(String path, String... parts)
  {
    try {
      String url = url() + "/" + path;
      for (String part : parts) {
        part = URLEncoder.encode(part, "UTF-8");
        url += "/" + part;
      }

      // logger.debug( "returning url... " + url );

      return url;
    }
    catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public SuiteConfigurator configurator()
  {
    return configurator;
  }

}
