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
import java.util.Random;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.http.server.api.Behaviour;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * {@link Behaviour} that generates random count of bytes as response.
 *
 * @author cstamas
 * @since 0.8
 */
public class GenerateRandomBehaviour
    extends BehaviourSupport
{
  private final Random random = new Random();

  private static final byte[] bytes = new byte[1024];

  private final int length;

  /**
   * Constructor.
   *
   * @param length the length of the response in bytes.
   */
  public GenerateRandomBehaviour(final int length)
  {
    checkArgument(length > 0, "Length must be greater than zero!");
    this.length = length;
  }

  public boolean execute(final HttpServletRequest request, final HttpServletResponse response,
                         final Map<Object, Object> ctx)
      throws Exception
  {
    if ("GET".equals(request.getMethod())) {
      response.setContentType("application/octet-stream");
      response.setContentLength(length);

      ServletOutputStream out = response.getOutputStream();
      for (int i = length; i > 0; ) {
        random.nextBytes(bytes);
        int n = Math.min(i, bytes.length);
        i -= n;
        out.write(bytes, 0, n);
      }
      out.close();
      return false;
    }

    return true;
  }
}
