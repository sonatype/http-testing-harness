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
public class Retry
    extends BehaviourSupport
{

  private final AtomicInteger counter = new AtomicInteger(0);

  private int retry = -1;

  private int error = 404;

  public Retry(int error)
  {
    this.error = error;
  }

  public Retry(int error, int retry)
  {
    this(error);
    this.retry = retry;
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (retry == -1) {
      String path = request.getPathInfo().substring(1);
      String[] split = path.split("/", 2);
      retry = Integer.valueOf(split[0]).intValue();
    }
    if (counter.incrementAndGet() < retry) {
      response.sendError(error);
      return false;
    }

    return true;
  }

}
