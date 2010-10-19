package org.sonatype.tests.jetty.server.behaviour.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;
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
public class Get
    implements Behaviour
{

    private String fpath = ".";

    public void setPath( String fpath )
    {
        this.fpath = fpath;
    }

    public Get()
    {
        super();
    }

    public Get( String path )
    {
        Log.debug( "Starting FileServer with base path " + new File( path ).getAbsolutePath() );
        this.fpath = path;
    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( "GET".equals( request.getMethod() ) )
        {
            String path = request.getPathInfo();
            File file = new File( fpath, path );

            Log.debug( "getting " + file.getAbsolutePath() );

            if ( !file.canRead() )
            {
                Log.debug( "Cannot read: " + file.getPath() );
                response.sendError( 404 );
                return false;
            }

            Log.debug( "Delivering: " + file.getPath() );
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
            return false;
        }

        return true;
    }

}
