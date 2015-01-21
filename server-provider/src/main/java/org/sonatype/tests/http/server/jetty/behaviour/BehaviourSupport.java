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

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.sonatype.tests.http.server.api.Behaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BehaviourSupport
    implements Behaviour
{
  protected final Logger log = LoggerFactory.getLogger(getClass());

  public static String firstPart( String path )
  {
    return path.substring( 1 ).split( "/", 2 )[0];
  }

  public static String lastPart( String path )
  {
    String[] split = path.substring( 1 ).split( "/" );
    return split[split.length - 1];
  }

  public static String pathAsContent( String path )
  {
    return path.substring( 1 ).replaceFirst( "[^/]*/", "" );
  }

  public static void setContent( String content, Map<Object, Object> ctx )
      throws UnsupportedEncodingException
  {
    ctx.put( Behaviour.Keys.CONTENT, content );
    ctx.put( Behaviour.Keys.CONTENT_SIZE, content.getBytes( "UTF-8" ).length );
  }

}
