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
package org.sonatype.tests.http.server.jetty.impl;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.http.server.fluent.Proxy;
import org.sonatype.tests.http.server.jetty.behaviour.ProxyAuth;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;


/**
 * This is NOT a HTTP Proxy, it simply mimics it at connection and auth level, but it's still plain
 * server provider. See {@link Proxy}.
 *
 * @author Benjamin Hanzelmann
 */
public class JettyProxyProvider
    extends JettyServerProvider
{

  /**
   * @author Benjamin Hanzelmann
   */
  public class ProxyAuthHandler
      extends ConstraintSecurityHandler
  {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
      try {
        boolean authenticated = true;
        if (user != null) {
          authenticated = new ProxyAuth(user, password).execute(request, response, null);
        }

        if (authenticated) {
          super.handle(target, baseRequest, request, response);
        }
        else {
          baseRequest.setHandled(true);
        }
      }
      catch (Exception e) {
        throw new ServletException(e.getMessage(), e);
      }
    }

  }

  private String password;

  private String user;

  public JettyProxyProvider()
  {
  }

  public JettyProxyProvider(String user, String pw)
      throws Exception
  {
    this.user = user;
    this.password = pw;
  }

  @Override
  public void addAuthentication(String pathSpec, String authName)
  {
    setSecurityHandler(new ProxyAuthHandler());
    super.addAuthentication(pathSpec, authName);
  }

  @Override
  protected void initWebappContext(Server s)
  {
    super.initWebappContext(s);
    if (user != null) {
      ProxyAuthHandler pah = new ProxyAuthHandler();
      getWebappContext().setSecurityHandler(pah);
    }
  }

}
