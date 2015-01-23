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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.http.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 */
public class Stutter
    extends BehaviourSupport
{

  private int wait = -1;

  private byte[] content;

  public Stutter()
  {
    super();
  }

  public Stutter(int i, byte[] content)
  {
    this.wait = i;
    this.content = content;
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.tests.jetty.server.api.Behaviour#execute(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse, java.util.Map)
   */
  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (wait != -1) {
      response.setContentLength(content.length);
      ServletOutputStream out = response.getOutputStream();
      for (int i = 0; i < content.length; i++) {
        out.write(content[i]);
        out.flush();
        Thread.sleep(wait);
      }
      return false;
    }
    else {
      String path = request.getPathInfo().substring(1);
      String[] split = path.split("/", 2);
      Integer time = Integer.valueOf(split[0]);

      String[] msgs = split[1].split("/");
      ctx.put(Behaviour.Keys.STUTTER_MSGS, msgs);

      int size = 0;
      for (String string : msgs) {
        size += string.getBytes("UTF-8").length;
      }
      response.setContentLength(size);

      for (String msg : msgs) {
        try {
          Thread.sleep(time);
          response.getWriter().write(msg);
          response.getWriter().flush();
          response.flushBuffer();
        }
        catch (Exception e) {
          e.printStackTrace();
          throw new IllegalStateException("Stutter Behaviour failing: " + e.getMessage(), e);
        }
      }
      return true;
    }
  }

}
