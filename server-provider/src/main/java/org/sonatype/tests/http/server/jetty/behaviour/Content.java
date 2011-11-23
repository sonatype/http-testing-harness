package org.sonatype.tests.http.server.jetty.behaviour;

/*
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

import org.sonatype.tests.http.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 */
public class Content
    implements Behaviour
{

    private String content;

    private String type;

    public static Content content( String content )
    {
        return new Content( content );
    }

    public static Content content( String content, String type )
    {
        return new Content( content, type );
    }

    public Content()
    {
        this.type = "text/plain";
    }

    public Content( String content )
    {
        this( content, "text/plain" );
    }

    public Content( String content, String type )
    {
        this.content = content;
        this.type = type;
    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( "GET".equals( request.getMethod() ) )
        {
            String content = this.content;
            response.setContentType( type );

            if ( content == null )
            {
                String pathInfo = request.getPathInfo();
                content = pathInfo == null ? "" : pathInfo.substring( 1 );
            }

            if ( ctx.containsKey( Behaviour.Keys.CONTENT ) )
            {
                content = ctx.get( Behaviour.Keys.CONTENT ).toString();
            }

            response.setContentLength( content.getBytes( "UTF-8" ).length );
            try
            {
                response.getOutputStream().write( content.getBytes( "UTF-8" ) );
            }
            catch ( IllegalStateException e )
            {
                response.getWriter().write( content );
            }
            return false;
        }
        else
        {
            return true;
        }
    }

}
