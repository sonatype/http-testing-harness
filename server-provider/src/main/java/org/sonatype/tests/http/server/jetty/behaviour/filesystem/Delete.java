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

public class Delete
    extends FSBehaviour
{

  private boolean really = false;

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    log.warn("delete method: {}", request.getMethod());
    if (!"DELETE".equals(request.getMethod())) {
      return true;
    }

    int code = 200;

    File file = fs(request.getPathInfo());
    if (!file.exists()) {
      log.debug("Delete: File does not exist: {}", file.getAbsolutePath());
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return false;
    }

    if (really && (!file.delete())) {
      response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return false;
    }

    response.setStatus(code);

    return false;
  }

  public Delete(File file)
  {
    super(file);
  }

  public Delete(String path)
  {
    super(path);
  }

  public Delete(File file, boolean reallyDelete)
  {
    super(file);
    this.really = reallyDelete;
  }

  public Delete(String path, boolean reallyDelete)
  {
    super(path);
    this.really = reallyDelete;
  }

}
