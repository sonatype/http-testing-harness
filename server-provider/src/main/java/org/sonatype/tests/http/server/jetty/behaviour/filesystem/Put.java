package org.sonatype.tests.http.server.jetty.behaviour.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class Put
    extends FSBehaviour
{

    public Put( File file )
    {
        super( file );
    }

    public Put( String path )
    {
        super( path );
    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( !"PUT".equals( request.getMethod() ) )
        {
            return true;
        }

        File fsFile = fs( request.getPathInfo() );

        int code = fsFile.exists() ? 200 : 201;

        fsFile.getParentFile().mkdirs();
        fsFile.createNewFile();

        if ( !fsFile.canWrite() )
        {
            code = 405;
            response.sendError( code );
            return false;
        }
        else
        {

            ServletInputStream in = request.getInputStream();
            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream( fsFile );
                IOUtils.copy( in, out );
            }
            finally
            {
                IOUtils.closeQuietly( in );
                IOUtils.closeQuietly( out );
            }
        }

        response.setStatus( code );
        return false;
    }

}
