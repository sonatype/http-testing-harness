package org.sonatype.tests.jetty.server.behaviour.filesystem;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;

public class Head
    extends FSBehaviour
{

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( !"HEAD".equals( request.getMethod() ) )
        {
            return true;
        }

        int code = 200;

        if ( !fs( request.getPathInfo() ).exists() )
        {
            Log.debug( fs( request.getPathInfo() ) + " does not exist, sending error" );
            code = HttpServletResponse.SC_NOT_FOUND;
            response.setStatus( code );

            return false;
        }
        Log.debug( fs( request.getPathInfo() ) + " exists, sending code " + code );
        response.setStatus( code );


        return false;
    }

    public Head( File file )
    {
        super( file );
    }

    public Head( String path )
    {
        super( path );
    }

}
