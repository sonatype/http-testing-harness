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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.http.server.jetty.behaviour.BehaviourSupport;

import com.google.common.io.ByteStreams;


/**
 * @author Benjamin Hanzelmann
 */
public class Get
    extends BehaviourSupport
{

  private String fpath = ".";

  public void setPath(String fpath) {
    this.fpath = fpath;
  }

  public static Get get(File root) {
    return new Get(root.getAbsolutePath());
  }

  public Get() {
    super();
  }

  public Get(String path) {
    this.fpath = path;
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if ("GET".equals(request.getMethod())) {
      String path = request.getPathInfo();
      File file = new File(fpath, path);

      log.debug("GETting {}", file.getAbsolutePath());

      if (!file.isFile() || !file.canRead()) {
        log.debug("Cannot read: {}", file.getPath());
        response.sendError(404);
        return false;
      }

      log.debug("Delivering: {}", file.getPath());
      response.setContentLength((int) file.length());
      response.setDateHeader("Last-modified", file.lastModified());

      try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
        ByteStreams.copy(in, out);
      }

      return false;
    }

    return true;
  }

}
