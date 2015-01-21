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
package org.sonatype.tests.http.server.jetty.behaviour.pgp;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.jetty.behaviour.BehaviourSupport;

import com.google.common.collect.Maps;

/**
 * {@link Behaviour} emulating PGP SKS.
 */
public class KeyBehaviour
    extends BehaviourSupport
{
  private final Map<String, String> keys = Maps.newLinkedHashMap();

  public void addKey(Long key, String content) {
    keys.put(("0x" + Long.toHexString(key)).toLowerCase(), content);
  }

  @Override
  public boolean execute(final HttpServletRequest request, final HttpServletResponse response,
                         final Map<Object, Object> ctx)
      throws Exception
  {
    final String key = request.getParameter("search");
    final String result = keys.get(key.toLowerCase());

    if (result == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such key");
      return false;
    }

    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);
    response.setBufferSize(result.length());
    response.setHeader("Server", "sks_www/1.1.0");
    response.getWriter().print(result);
    return false;
  }
}
