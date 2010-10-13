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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 */
public class Stutter
    implements Behaviour
{

    private int wait = -1;

    private byte[] content;

    public Stutter()
    {
        super();
    }

    public Stutter( int i, byte[] content )
    {
        this.wait = i;
        this.content = content;
    }

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( wait == -1 )
        {
            String path = request.getPathInfo().substring( 1 );
            String[] split = path.split( "/", 2 );
            Integer time = Integer.valueOf( split[0] );

            ctx.put( Behaviour.Keys.STUTTER_TIME, time );
            String[] msgs = split[1].split( "/" );
            ctx.put( Behaviour.Keys.STUTTER_MSGS, msgs );

            Integer ctxSize = (Integer) ctx.get( Behaviour.Keys.CONTENT_SIZE );
            int size = ctxSize == null ? 0 : ctxSize;
            for ( String string : msgs )
            {
                size += string.getBytes( "UTF-8" ).length;
            }

            ctx.put( Behaviour.Keys.CONTENT_SIZE, size );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.tests.jetty.server.api.Behaviour#execute(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, java.util.Map)
     */
    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        Integer time = (Integer) ctx.get( Behaviour.Keys.STUTTER_TIME );
        String[] msgs;
        if ( wait != -1 )
        {
            time = Integer.valueOf( wait );

            response.setContentLength( content.length );
            ServletOutputStream out = response.getOutputStream();
            System.err.println( "writing " + new String( content, "UTF-8" ) );
            for ( int i = 0; i < content.length; i++ )
            {
                out.write( content[i] );
                Thread.sleep( wait );
            }
            return false;
        }
        else
        {
            msgs = (String[]) ctx.get( Behaviour.Keys.STUTTER_MSGS );

            for ( String msg : msgs )
            {
                response.setContentLength( (Integer) ctx.get( Behaviour.Keys.CONTENT_SIZE ) );
                try
                {

                    Thread.sleep( time );
                    response.getWriter().write( msg );
                    response.getWriter().flush();
                    response.flushBuffer();
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    throw new IllegalStateException( "Stutter Behaviour failing: " + e.getMessage(), e );
                }
            }
            return true;
        }
    }

}
