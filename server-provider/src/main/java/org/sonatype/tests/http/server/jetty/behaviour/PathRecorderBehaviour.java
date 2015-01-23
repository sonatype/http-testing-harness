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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.http.server.api.Behaviour;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * {@link Behaviour} implementation that records request path(s) (URIs) for processed HTTP verb. The URIs are in order
 * for given HTTP verb, as they did income. This behaviour, while does similar thing as {@link Record}, it differs from
 * it that here, paths are reusable and stored as is (Record combines verb and path and other info into one string),
 * and
 * this class does not reorder request paths, it keeps their "income" order (Recorder reverts the list, last request
 * becomes first).
 *
 * @author cstamas
 * @since 0.8
 */
public class PathRecorderBehaviour
    extends BehaviourSupport
{
  private final Multimap<String, String> pathsMap = ArrayListMultimap.create();

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    final String path = request.getRequestURI();
    final String verb = request.getMethod();
    pathsMap.put(verb, path);
    return true;
  }

  public List<String> getPathsForVerb(final String verb)
  {
    return new ArrayList<String>(pathsMap.get(verb));
  }

  public void clear()
  {
    pathsMap.clear();
  }
}
