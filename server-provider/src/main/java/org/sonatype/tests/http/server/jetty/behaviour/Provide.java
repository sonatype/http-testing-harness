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
package org.sonatype.tests.http.server.jetty.behaviour;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Benjamin Hanzelmann
 */
public class Provide
    extends BehaviourSupport
{

  private final Map<String, byte[]> db = new ConcurrentHashMap<String, byte[]>();

  private int latency = -1;

  public void addPath(String path, byte[] content)
  {
    this.db.put(path, content);
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    String path = request.getPathInfo().substring(1);
    log.debug("{} {}", request.getMethod(), path);

    if ("GET".equals(request.getMethod())) {
      byte[] ba = db.get(path);
      if (ba == null) {
        ba = new byte[0];
      }

      response.setContentType("application/octet-stream");
      response.setContentLength(ba.length);

      ServletOutputStream out = response.getOutputStream();
      for (int i = 0; i < ba.length; i++) {
        out.write(ba[i]);
        out.flush();
        if (latency != -1) {
          Thread.sleep(latency);
        }
      }
      out.close();
      return false;
    }

    return true;
  }

  public void setLatency(int i)
  {
    this.latency = i;

  }

}
