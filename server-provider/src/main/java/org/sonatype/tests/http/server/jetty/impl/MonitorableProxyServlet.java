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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;

/**
 * A real HTTP proxy servlet with ability to gather
 * accessed URIs accessed via this proxy.
 */
public class MonitorableProxyServlet
    extends org.eclipse.jetty.proxy.ProxyServlet
{
  private final List<String> accessedUris;

  private final Map<String, String> authentications;

  private final boolean useAuthentication;

  public MonitorableProxyServlet() {
    this(false, null);
  }

  public MonitorableProxyServlet(final boolean useAuthentication, final Map<String, String> authentications) {
    this.useAuthentication = useAuthentication;
    this.authentications = Maps.newHashMap();
    if (authentications != null) {
      this.authentications.putAll(authentications);
    }
    this.accessedUris = Lists.newArrayList();
  }

  public List<String> getAccessedUris() {
    return accessedUris;
  }

  @Override
  public void service(ServletRequest req, ServletResponse res)
      throws ServletException,
             IOException
  {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    if (useAuthentication) {
      String proxyAuthorization = request.getHeader("Proxy-Authorization");
      if (proxyAuthorization != null && proxyAuthorization.startsWith("Basic ")) {
        String proxyAuth = proxyAuthorization.substring(6);
        String authorization = B64Code.decode(proxyAuth, StringUtil.__ISO_8859_1);
        String[] authTokens = authorization.split(":");
        String user = authTokens[0];
        String password = authTokens[1];

        String authPass = authentications.get(user);
        if (!password.equals(authPass)) {
          // Proxy-Authenticate Basic realm="CCProxy Authorization"
          response.addHeader("Proxy-Authenticate", "Basic realm=\"Jetty Proxy Authorization\"");
          response.setStatus(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
          return;
        }
      }
    }
    String uri = ((Request) req).getHttpURI().toString();
    getAccessedUris().add(uri);
    super.service(req, res);
  }
}
