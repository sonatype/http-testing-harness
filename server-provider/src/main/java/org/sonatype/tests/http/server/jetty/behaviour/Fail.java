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
public class Fail
    extends BehaviourSupport
{

  private String message = null;

  private int code = -1;

  private int count = 0;

  private int numFailures = -1;

  private final AtomicInteger failed = new AtomicInteger(0);

  public Fail(int count, int code, String message)
  {
    this.numFailures = count;
    this.code = code;
    this.message = message;
  }

  public Fail(int code, String message)
  {
    this(-1, code, message);
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (numFailures == -1) {
      failed.incrementAndGet();
      log.debug("Always failing: {}", failed);
      response.sendError(code, message);
      return false;
    }

    if (count++ < numFailures) {
      failed.incrementAndGet();
      log.debug("failing {} times: {}", count, failed);
      response.sendError(code, message);
      return false;
    }

    count = 0;
    return true;
  }

  public int getFailedCount()
  {
    return failed.get();
  }

}
