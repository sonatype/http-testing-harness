package org.sonatype.tests.jetty.server.behaviour.filesystem;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;

public class Delete
    extends FSBehaviour
{

    private boolean really = false;

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        Log.warn( "delete method: " + request.getMethod() );
        if ( !"DELETE".equals( request.getMethod() ) )
        {
            return true;
        }

        int code = 200;

        File file = fs( request.getPathInfo() );
        if ( ! file.exists() )
        {
            Log.debug( "Delete: File does not exist: " + file.getAbsolutePath() );
            response.setStatus( HttpServletResponse.SC_NOT_FOUND );
            return false;
        }

        if ( really && ( !file.delete() ) )
        {
            response.setStatus( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
            return false;
        }

        response.setStatus( code );

        return false;
    }

    public Delete( File file )
    {
        super( file );
    }

    public Delete( String path )
    {
        super( path );
    }

    public Delete( File file, boolean reallyDelete )
    {
        super( file );
        this.really = reallyDelete;
    }

    public Delete( String path, boolean reallyDelete )
    {
        super( path );
        this.really = reallyDelete;
    }

}
