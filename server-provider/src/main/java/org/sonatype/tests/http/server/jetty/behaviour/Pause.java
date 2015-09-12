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

import org.sonatype.goodies.common.Time;

/**
 * @author Benjamin Hanzelmann
 */
public class Pause
    extends BehaviourSupport
{

  private long pause = -1;

  public static Pause pause(Time time)
  {
    return new Pause(time.toMillis());
  }

  public Pause()
  {
    super();
  }

  public Pause(long pause)
  {
    this.pause = pause;
  }

  public Pause(int pause)
  {
    this.pause = pause;
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
  {
    if (pause == -1) {
      String path = request.getPathInfo().substring(1);
      String[] split = path.split("/", 2);
      pause = Integer.valueOf(split[0]).intValue();
    }
    try {
      Thread.sleep(pause);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    return true;
  }

}
