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

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Benjamin Hanzelmann
 */
public class Deliver
    extends BehaviourSupport
{

  private File file;

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (file == null) {
      String path = pathAsContent(request.getPathInfo());
      file = new File(path);
    }
    ServletOutputStream out = response.getOutputStream();
    FileInputStream in = null;
    try {
      response.setContentLength((int) file.length());
      in = new FileInputStream(file);

      int read = -1;
      byte[] buf = new byte[16000];
      while ((read = in.read(buf)) != -1) {
        out.write(buf, 0, read);
      }
    }
    finally {
      if (in != null) {
        in.close();
      }
      out.close();
    }

    return false;
  }

  public Deliver(File file)
  {
    this.file = file;
  }

}
