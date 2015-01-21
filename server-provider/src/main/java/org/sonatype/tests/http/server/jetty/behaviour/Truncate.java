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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Benjamin Hanzelmann
 */
public class Truncate
    extends BehaviourSupport
{

  private int count = -1;

  public Truncate()
  {
    super();
  }

  public Truncate(int count)
  {
    this.count = count;
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    String path = request.getPathInfo().substring(1);
    String msg = null;
    if (count == -1) {
      String[] split = path.split("/", 2);
      count = Integer.valueOf(split[0]).intValue();
      msg = split[1].substring(0, count);
    }

    response.setContentLength(path.getBytes("UTF-8").length);
    if (msg != null) {
      response.getWriter().write(msg);
    }
    else {
      response.getWriter().write(request.getPathInfo().substring(1, count + 1));
    }
    return false;
  }

}
