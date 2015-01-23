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
package org.sonatype.tests.http.server.jetty.configurations;

import org.sonatype.tests.http.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
public abstract class AuthSslSuiteConfigurator
    extends SslSuiteConfigurator
{

  private String authName;

  public AuthSslSuiteConfigurator(String authName)
  {
    this.authName = authName;
  }

  @Override
  public ServerProvider provider()
  {
    ServerProvider p = super.provider();
    p.addAuthentication("/*", authName);
    p.addUser("user", "password");
    return p;
  }

  @Override
  public String getName()
  {
    return super.getName() + " Authentication";
  }

}
