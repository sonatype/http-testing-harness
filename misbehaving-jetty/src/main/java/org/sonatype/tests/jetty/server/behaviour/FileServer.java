package org.sonatype.tests.jetty.server.behaviour;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.tests.server.api.Behaviour;

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


/**
 * @author Benjamin Hanzelmann
 */
public class FileServer
    implements Behaviour
{

    private Logger logger = LoggerFactory.getLogger( FileServer.class );

    private String fpath = ".";

    public void setPath( String fpath )
    {
        this.fpath = fpath;
    }

    public FileServer()
    {
        super();
    }

    public FileServer( String path )
    {
        this.fpath = path;
    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        String path = request.getPathInfo();
        File file = new File( fpath, path );

        logger.debug( "GET " + path );
        logger.debug( "getting " + file.getAbsolutePath() );

        if ( "GET".equals( request.getMethod() ) )
        {
            if ( !file.canRead() )
            {
                response.sendError( 404 );
                return false;
            }
            
            response.setContentLength( (int) file.length() );
            FileInputStream in = null;
            try
            {
                in = new FileInputStream( file );
                ServletOutputStream out = response.getOutputStream();
                byte[] b = new byte[16000];
                int count;
                while ( ( count = in.read( b ) ) != -1 )
                {
                    out.write( b, 0, count );
                }
                out.close();
            }
            finally
            {
                if ( in != null )
                {
                    in.close();
                }
            }
        }

        return false;
    }

}
