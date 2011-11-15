package org.sonatype.tests.http.server.jetty.behaviour.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Post
    extends FSBehaviour
{

    public Post( File file )
    {
        super( file );
    }

    public Post( String path )
    {
        super( path );
    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( !"POST".equals( request.getMethod() ) )
        {
            return true;
        }

        File fsFile = fs( request.getPathInfo() );

        int code = 200;
        if ( !fsFile.exists() )
        {
            code = 201;
        }

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


                byte[] b = new byte[16000];
                int count = -1;
                while ( ( count = in.read( b ) ) != -1 )
                {
                    out.write( b, 0, count );
                }

            }
            finally
            {
                if ( in != null )
                {
                    in.close();
                }
                if ( out != null )
                {
                    out.close();
                }
            }

        }

        response.setStatus( code );
        return false;
    }

}
