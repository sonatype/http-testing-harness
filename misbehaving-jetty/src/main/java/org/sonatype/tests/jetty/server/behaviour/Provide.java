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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.tests.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class Provide
    implements Behaviour
{

    private static Logger logger = LoggerFactory.getLogger( Provide.class );

    private Map<String, byte[]> db = new HashMap<String, byte[]>();

    public void addPath( String path, byte[] content )
    {
        this.db.put( path, content );
    }

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {

    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        String path = request.getPathInfo().substring( 1 );
        logger.debug( request.getMethod() + " " + path );

        if ( "GET".equals( request.getMethod() ) )
        {
            byte[] ba = db.get( path );

            logger.debug( "sending " + Arrays.toString( ba ) );

            response.setContentType( "application/octet-stream" );
            response.setContentLength( ba.length );

            ServletOutputStream out = response.getOutputStream();
            out.write( ba );
            out.close();
            logger.debug( "sent " + Arrays.toString( ba ) );
            return false;
        }

        return true;
    }

}
