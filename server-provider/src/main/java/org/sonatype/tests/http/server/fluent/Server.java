/*
 * Copyright (c) 2010-2012 Sonatype, Inc. All rights reserved.
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

    public Server withKeystore(final String keystore, final String password)
    {
        serverProvider.setSSL( keystore, password );
        return this;
    }

    /**
     * @since 0.8
     */
    public Server withTruststore( final String truststore, final String password )
    {
        serverProvider.setSSLTruststore( truststore, password );
        return this;
    }

    /**
     * @since 0.8
     */
    public Server requireClientAuth()
    {
        serverProvider.setSSLNeedClientAuth( true );
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
