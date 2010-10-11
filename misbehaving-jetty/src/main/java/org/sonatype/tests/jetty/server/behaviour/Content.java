package org.sonatype.tests.jetty.server.behaviour;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class Content
    implements Behaviour
{

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        String path = request.getPathInfo().substring( 1 );
        String content = path;
        if ( "GET".equals( request.getMethod() ) )
        {
            BehaviourHelper.setContent( content, ctx );
        }
    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        String content = (String) ctx.get( Behaviour.Keys.CONTENT );
        if ( "GET".equals( request.getMethod() ) )
        {
            response.setContentLength( content.getBytes( "UTF-8" ).length );
            response.getWriter().write( content );
        }
        else
        {
            response.getWriter().close();
        }
        return true;
    }

}
