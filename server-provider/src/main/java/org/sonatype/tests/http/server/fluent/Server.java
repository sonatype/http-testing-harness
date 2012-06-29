package org.sonatype.tests.http.server.fluent;

import java.net.MalformedURLException;
import java.net.URL;

import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

/**
 * @since 0.4.3
 */
public class Server
{

    private final ServerProvider serverProvider;

    public Server()
        throws Exception
    {
        this.serverProvider = new JettyServerProvider();
    }

    public Server( final ServerProvider serverProvider )
    {
        this.serverProvider = serverProvider;
    }

    /**
     * Start a server on a random port.
     * @since 0.6
     */
    public static Server server()
        throws Exception
    {
        return new Server();
    }

    /**
     * Start a server on the given port (0 for random).
     */
    public static Server withPort(int port)
        throws Exception
    {
        JettyServerProvider jettyServerProvider = new JettyServerProvider();
        jettyServerProvider.setPort( port );
        return new Server(jettyServerProvider);
    }

    /**
     * Set the port for the server.
     * @throws IllegalStateException if the server is already started.
     * @since 0.6
     */
    public Server port(int port)
        throws IllegalStateException
    {
        if ( serverProvider.isStarted() )
        {
            throw new IllegalStateException( "Server is currently running, cannot change port." );
        }
        serverProvider.setPort( port );

        return this;
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
