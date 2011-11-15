package org.sonatype.tests.http.server.jetty.behaviour.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.log.Log;
import org.sonatype.tests.http.server.api.Behaviour;

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

            InputStream in = null;
            OutputStream out = null;
            try
            {
                in = new FileInputStream( file );
                out = response.getOutputStream();
                IOUtils.copy( in, out );
            }
            finally
            {
                IOUtils.closeQuietly( in );
                IOUtils.closeQuietly( out );
            }

            return false;
        }

        return true;
    }

}
