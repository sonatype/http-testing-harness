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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Benjamin Hanzelmann
 */
public class Record
    extends BehaviourSupport
{

  private final Map<String, Map<String, String>> requestHeaders =
      new ConcurrentHashMap<String, Map<String, String>>();

  private final List<String> requests = Collections.synchronizedList(new LinkedList<String>());

  public Record()
  {
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    add(request);
    return true;
  }

  private void add(HttpServletRequest request)
  {
    String uri = request.getRequestURI();
    String req = request.getMethod() + " " + uri;
    requests.add(req);

    String pathInfo = request.getContextPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
    Map<String, String> list = requestHeaders.get(pathInfo);
    if (list == null) {
      list = new HashMap<String, String>();
      requestHeaders.put(pathInfo, list);
    }
    @SuppressWarnings("rawtypes")
    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement().toString();
      String value = request.getHeader(name);
      list.put(name, value);
    }
  }

  public Map<String, Map<String, String>> getRequestHeaders()
  {
    return requestHeaders;
  }

  public List<String> getRequests()
  {
    List<String> list = new ArrayList<String>(requests);
    Collections.reverse(list);
    return list;
  }

  public void clear()
  {
    requestHeaders.clear();
    requests.clear();
  }

}
