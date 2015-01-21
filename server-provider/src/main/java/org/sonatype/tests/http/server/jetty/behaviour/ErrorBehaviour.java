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
public class ErrorBehaviour
    extends BehaviourSupport
{

  private String msg;

  private int error = -1;

  public static ErrorBehaviour error(int code)
  {
    return new ErrorBehaviour(code, "error");
  }

  public static ErrorBehaviour error(int code, String msg)
  {
    return new ErrorBehaviour(code, msg);
  }

  public ErrorBehaviour()
  {
  }

  public ErrorBehaviour(int error, String msg)
  {
    this.error = error;
    this.msg = msg;
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (error == -1) {
      String path = request.getPathInfo().substring(1);
      String[] split = path.split("/", 2);
      int sc = Integer.valueOf(split[0]);
      if (split.length > 1) {
        msg = split[1];
      }
      else {
        msg = "errormsg";
      }
      response.sendError(sc, msg);
    }
    else {
      response.sendError(error, msg);
    }

    return false;

  }

}
