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

public class Post
    extends FSBehaviour
{

  public Post(File file)
  {
    super(file);
  }

  public Post(String path)
  {
    super(path);
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (!"POST".equals(request.getMethod())) {
      return true;
    }

    File fsFile = fs(request.getPathInfo());

    int code = 200;
    if (!fsFile.exists()) {
      code = 201;
    }

    fsFile.createNewFile();

    if (!fsFile.canWrite()) {
      code = 405;
      response.sendError(code);
      return false;
    }
    else {

      ServletInputStream in = request.getInputStream();
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(fsFile);


        byte[] b = new byte[16000];
        int count = -1;
        while ((count = in.read(b)) != -1) {
          out.write(b, 0, count);
        }

      }
      finally {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }

    }

    response.setStatus(code);
    return false;
  }

}
