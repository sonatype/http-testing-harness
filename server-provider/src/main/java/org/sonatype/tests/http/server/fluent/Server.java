package org.sonatype.tests.http.server.fluent;

import java.net.MalformedURLException;
import java.net.URL;

import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

/**
 * @since 1.0
 */
public class Server
{

    private final ServerProvider serverProvider;

    public Server( final ServerProvider serverProvider )
    {
        this.serverProvider = serverProvider;
    }

    public static Server withPort(int port)
        throws Exception
    {
        JettyServerProvider jettyServerProvider = new JettyServerProvider();
        jettyServerProvider.setPort( port );
        return new Server(jettyServerProvider);
    }

    public ServeContext serve(String pattern)
    {
        return new ServeContext(serverProvider, pattern, this );
    }

    public Server start()
        throws Exception
    {
        serverProvider.start();
        return this;
    }

    public void stop()
        throws Exception
    {
        serverProvider.stop();
    }

    public int getPort()
    {
        return serverProvider.getPort();
    }

    public URL getUrl()
        throws MalformedURLException
    {
        return serverProvider.getUrl();
    }

}
