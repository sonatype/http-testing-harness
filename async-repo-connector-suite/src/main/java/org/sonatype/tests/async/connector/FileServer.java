package org.sonatype.tests.async.connector;

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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
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
public class FileServer
    implements Behaviour
{

    private static Logger logger = LoggerFactory.getLogger( FileServer.class );

    public Map<String, byte[]> db = new HashMap<String, byte[]>();

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {

    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        String path = request.getPathInfo();
        logger.debug( request.getMethod() + " " + path );
        if ( "GET".equals( request.getMethod() ) )
        {
            if ( !db.containsKey( path ) )
            {
                response.sendError( HttpServletResponse.SC_NOT_FOUND, "Not Found" );
                return false;
            }

            byte[] bs = db.get( path );
            response.setContentLength( bs.length );
            response.setContentType( "application/octet-stream" );
            ServletOutputStream out = response.getOutputStream();
            out.write( bs );
            out.close();
        }
        else if ( "PUT".equals( request.getMethod() ) )
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int count = -1;
            ServletInputStream in = request.getInputStream();
            byte[] b = new byte[16000];
            while ( ( count = in.read( b ) ) != -1 )
            {
                out.write( b, 0, count );
            }
            out.close();
            db.put( path, out.toByteArray() );
        }
        return false;
    }

}
