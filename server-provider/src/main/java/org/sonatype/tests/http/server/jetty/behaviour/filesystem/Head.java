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
package org.sonatype.tests.http.server.jetty.behaviour.filesystem;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Head
    extends FSBehaviour
{

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (!"HEAD".equals(request.getMethod())) {
      return true;
    }

    int code = 200;

    if (!fs(request.getPathInfo()).exists()) {
      log.debug("{} does not exist, sending error", fs(request.getPathInfo()));
      code = HttpServletResponse.SC_NOT_FOUND;
      response.setStatus(code);
    }
    else {
      log.debug("{} exists, sending code {}", fs(request.getPathInfo()), code);
      response.setStatus(code);
      response.setDateHeader("Last-modified", fs(request.getPathInfo()).lastModified());
    }

    return false;
  }

  public Head(File file)
  {
    super(file);
  }

  public Head(String path)
  {
    super(path);
  }

}
