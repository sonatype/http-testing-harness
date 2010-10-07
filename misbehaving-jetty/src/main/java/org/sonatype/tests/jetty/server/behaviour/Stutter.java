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

import org.sonatype.tests.jetty.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class Stutter
    implements Behaviour
{

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx ) throws Exception
    {
        String path = request.getPathInfo().substring( 1 );
        String[] split = path.split( "/", 2 );
        Integer time = Integer.valueOf( split[0] );

        ctx.put( Behaviour.Keys.STUTTER_TIME, time );
        String[] msgs = split[1].split( "/" );
        ctx.put( Behaviour.Keys.STUTTER_MSGS, msgs );
        
        Integer ctxSize = (Integer) ctx.get( Behaviour.Keys.CONTENT_SIZE );
        int size = ctxSize == null ? 0 : ctxSize;
        for (String string : msgs) {
            size += string.getBytes( "UTF-8" ).length;
        }

        ctx.put( Behaviour.Keys.CONTENT_SIZE, size );
        
    }

    /* (non-Javadoc)
     * @see org.sonatype.tests.jetty.server.api.Behaviour#execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Map)
     */
    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        Integer time = (Integer) ctx.get( Behaviour.Keys.STUTTER_TIME );
        String[] msgs = (String[]) ctx.get( Behaviour.Keys.STUTTER_MSGS );
        
        for (String msg : msgs) {
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
