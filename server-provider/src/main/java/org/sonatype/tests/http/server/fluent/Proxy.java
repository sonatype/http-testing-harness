package org.sonatype.tests.http.server.fluent;

import java.util.jar.JarEntry;

import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.impl.JettyProxyProvider;

/**
 * This is not a real proxy. It supports proxy connections and auth, but will not forward requests to the requested URL.
 * It will resolve the requested path against the behaviors set for the proxy instance.
 * <p/>
 * Example:
 * <pre>
 *     Server proxy = Proxy.withPort(0).start();
 *     proxy.serve( "/*" ).withBehaviours( redirect( "/other/url" ) );
 *     proxy.serve( "/other/*" ).withBehaviours( content( "test" ) );
 *     setProxy("localhost", proxy.getPort() );
 *     assertThat( get( "http://invalid.url/foo" ), is ( "test" ) );
 * </pre>
 */
public class Proxy
    extends Server
{

    public Proxy( final JettyProxyProvider serverProvider )
    {
        super( serverProvider );
    }

    public static Server withPort( int port )
        throws Exception
    {
        JettyProxyProvider provider = new JettyProxyProvider();
        provider.setPort( port );
        return new Server( provider );
    }

}
