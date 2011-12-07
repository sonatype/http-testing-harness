package org.sonatype.tests.http.server.fluent;

import java.io.File;

import org.sonatype.sisu.goodies.common.Time;
import org.sonatype.tests.http.server.jetty.behaviour.Content;
import org.sonatype.tests.http.server.jetty.behaviour.ErrorBehaviour;
import org.sonatype.tests.http.server.jetty.behaviour.Pause;
import org.sonatype.tests.http.server.jetty.behaviour.Redirect;
import org.sonatype.tests.http.server.jetty.behaviour.filesystem.Get;

/**
 * @since 1.0
 */
public class Behaviours
{

    public static Redirect redirect( String url )
    {
        return Redirect.redirect( url );
    }

    public static Redirect redirect( String url, int status )
    {
        return Redirect.redirect( url, status );
    }

    public static ErrorBehaviour error( int code )
    {
        return ErrorBehaviour.error( code );
    }

    public static ErrorBehaviour error( int code, String msg )
    {
        return ErrorBehaviour.error( code, msg );
    }

    public static Pause pause( Time time )
    {
        return Pause.pause( time );
    }

    public static Content content( String content )
    {
        return Content.content( content );
    }

    public static Content content( String content, String type )
    {
        return Content.content( content, type );
    }

    public static Get get( File root )
    {
        return Get.get( root );
    }

}
