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

import org.sonatype.sisu.goodies.common.FormatTemplate;
import org.sonatype.tests.http.server.api.Behaviour;

/**
 * {@link Behaviour} that combines {@link ErrorBehaviour} and {@link Content} behaviours, by letting specifying
 * response
 * error code and body, thus allowing to simulate error pages too. And it uses {@link FormatTemplate} for body, so body
 * template is evaluated per request.
 *
 * @author cstamas
 * @since 0.8
 */
public class DeliverBodyTemplateBehaviour
    extends BehaviourSupport
{
  private final int code;

  private final String bodyContentType;

  private final FormatTemplate body;

  public DeliverBodyTemplateBehaviour(final int code, final String bodyContentType, final FormatTemplate body)
  {
    this.code = code;
    this.bodyContentType = bodyContentType;
    this.body = body;
  }

  public boolean execute(HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx)
      throws Exception
  {
    response.setStatus(code);
    response.setContentType(bodyContentType);
    final byte[] bodyPayload = body.evaluate().getBytes("UTF-8");
    response.setContentLength(bodyPayload.length);
    response.getOutputStream().write(bodyPayload);
    return true;
  }
}
