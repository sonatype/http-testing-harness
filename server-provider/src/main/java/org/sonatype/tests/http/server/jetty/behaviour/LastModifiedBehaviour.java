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

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.http.server.api.Behaviour;

/**
 * {@link Behaviour} implementation that overrides the Last-Modified response header, depending on the value
 * this instance has set.
 *
 * @author cstamas
 * @since 0.8
 */
public class LastModifiedBehaviour
    extends BehaviourSupport
{
  private Date lastModified;

  public LastModifiedBehaviour(final Date date)
  {
    setLastModified(date);
  }

  public void setLastModified(final Date when)
  {
    lastModified = when;
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    if (lastModified != null) {
      response.setDateHeader("last-modified", lastModified.getTime());
    }
    return true;
  }

}
