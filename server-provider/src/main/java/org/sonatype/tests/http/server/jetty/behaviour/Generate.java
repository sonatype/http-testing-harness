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
 * A behavior that writes a sequence of fixed length to the client upon a GET request.
 */
public class Generate
    extends BehaviourSupport
{

  private static final byte[] bytes = new byte[1024];

  private final Map<String, Long> lengths = new ConcurrentHashMap<String, Long>();

  public void addContent(String path, long length)
  {
    if (!path.startsWith("/")) {
      path = '/' + path;
    }
    lengths.put(path, Long.valueOf(length));
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if ("GET".equals(request.getMethod())) {
      String path = request.getPathInfo();

      Long length = lengths.get(path);

      if (length != null) {
        response.setContentType("application/octet-stream");
        response.setContentLength(length.intValue());

        ServletOutputStream out = response.getOutputStream();
        for (int i = length.intValue(); i > 0; ) {
          int n = Math.min(i, bytes.length);
          i -= n;
          out.write(bytes, 0, n);
        }
        out.close();

        return false;
      }
    }

    return true;
  }

}
