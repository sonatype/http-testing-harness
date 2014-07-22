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
import java.io.FileOutputStream;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

public class Put
    extends FSBehaviour
{

  public Put(File file) {
    super(file);
  }

  public Put(String path) {
    super(path);
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (!"PUT".equals(request.getMethod())) {
      return true;
    }

    File fsFile = fs(request.getPathInfo());

    int code = fsFile.exists() ? 200 : 201;

    fsFile.getParentFile().mkdirs();
    fsFile.createNewFile();

    if (!fsFile.canWrite()) {
      code = 405;
      response.sendError(code);
      return false;
    }
    else {
      try (ServletInputStream in = request.getInputStream(); FileOutputStream out = new FileOutputStream(fsFile)) {
        ByteStreams.copy(in, out);
      }
    }

    response.setStatus(code);
    return false;
  }

}
