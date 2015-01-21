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
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Benjamin Hanzelmann
 */
public class Redirect
    extends BehaviourSupport
{

  private int count = -1;

  private final AtomicInteger redirectCount = new AtomicInteger(0);

  private String content;

  private String target;

  private String pattern;

  private String replace;

  private int status = -1;

  public static Redirect redirect(String url)
  {
    return new Redirect(url);
  }

  public static Redirect redirect(String url, int status)
  {
    return new Redirect(url, status);
  }

  public Redirect(String pattern, String replace)
  {
    this.pattern = pattern;
    this.replace = replace;
  }

  public Redirect()
  {
    super();
  }

  public Redirect(int count)
  {
    this.count = count;
  }

  public Redirect(String url)
  {
    this.target = url;
  }

  public Redirect(String url, int status)
  {
    this.target = url;
    this.status = status;
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (target != null && status != -1) {
      response.setContentLength(0);
      response.setStatus(status);
      response.setHeader("Location", target);
      return false;
    }
    else if (target != null) {
      response.setContentLength(0);
      response.sendRedirect(target);
      return false;
    }

    if (pattern != null) {
      String path = request.getPathInfo();
      path = path.replaceAll(pattern, replace);

      response.sendRedirect(path);

      return false;
    }

    if (count == -1) {
      count = Integer.valueOf(firstPart(request.getPathInfo()));
      // save original content for last redirect
      content = pathAsContent(request.getPathInfo());
    }
    else if (content == null) {
      content = request.getPathInfo();
    }

    if (redirectCount.get() < count) {
      System.err.println("Redirecting... " + redirectCount);
      response.setContentLength(0);
      response.sendRedirect(String.valueOf(redirectCount.incrementAndGet()));
      return false;
    }

    setContent(content, ctx);

    count = -1;
    redirectCount.set(0);
    content = null;

    return true;
  }

}
