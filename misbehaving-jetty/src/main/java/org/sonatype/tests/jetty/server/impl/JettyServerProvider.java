package org.sonatype.tests.jetty.server.impl;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.Filter;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.FilterMapping;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.tests.jetty.server.behaviour.Content;
import org.sonatype.tests.jetty.server.behaviour.Pause;
import org.sonatype.tests.jetty.server.behaviour.Redirect;
import org.sonatype.tests.jetty.server.behaviour.Stutter;
import org.sonatype.tests.jetty.server.behaviour.Truncate;
import org.sonatype.tests.jetty.server.util.FileUtil;
import org.sonatype.tests.server.api.Behaviour;
import org.sonatype.tests.server.api.ServerProvider;
import org.sonatype.tests.server.api.TestServlet;

/**
 * @author Benjamin Hanzelmann
 */
public class JettyServerProvider
    implements ServerProvider
{

    private static Logger logger = LoggerFactory.getLogger( JettyServerProvider.class );

    protected Server server;

    protected int port = -1;

    protected boolean ssl;

    private String host = "localhost";

    private WebAppContext webappContext;

    private String root = "default-server-root";

    private String sslKeystorePassword;

    private String sslKeystore;

    private SecurityHandler securityHandler;

    private HashUserRealm securityRealm;

    public JettyServerProvider()
        throws Exception
    {
        super();
    }

    public void setSSL( String keystore, String password )
    {
        this.ssl = true;
        this.sslKeystore = keystore;
        this.sslKeystorePassword = password;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public void initServer()
        throws Exception
    {
        server = getServer();
    }

    public Server getServer()
        throws URISyntaxException
    {
        Server s = new Server();

        Connector connector;
        if ( ssl )
        {
            connector = sslConnector();
        }
        else
        {
            connector = connector();
        }

        s.setConnectors( new Connector[] { connector } );

        initWebappContext( s );

        addDefaultServices();

        return s;
    }

    public void addAuthentication( String pathSpec, String authName )
    {
        if ( server == null )
        {
            try
            {
                initServer();
            }
            catch ( Exception e )
            {
                throw new IllegalStateException( e );
            }
        }
        initAuthentication( pathSpec, authName );
    }

    private void initAuthentication( String pathSpec, String authName )
    {
        Constraint constraint = new Constraint();
        if ( authName == null )
        {
            authName = Constraint.__BASIC_AUTH;
        }
        constraint.setName( authName );

        constraint.setRoles( new String[] { "users" } );
        constraint.setAuthenticate( true );

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint( constraint );
        cm.setPathSpec( pathSpec );

        securityHandler = new SecurityHandler();

        securityRealm = new HashUserRealm( "Test Server" );

        securityHandler.setUserRealm( securityRealm );
        securityHandler.setConstraintMappings( new ConstraintMapping[] { cm } );
        webappContext.addHandler( securityHandler );
    }

    public void addUser( String user, String password )
    {
        securityRealm.put( user, password );
        securityRealm.addUserToRole( user, "users" );
    }

    private void addDefaultServices()
    {
        addServlet( new ErrorServlet() );
        addBehaviour( "/content/*", new Content() );
        addBehaviour( "/stutter/*", new Stutter() );
        addBehaviour( "/pause/*", new Pause(), new Content() );
        addBehaviour( "/truncate/*", new Truncate() );
        addBehaviour( "/timeout/*", new Pause() );
        addBehaviour( "/redirect/*", new Redirect(), new Content() );
    }

    public void addServlet( TestServlet servlet )
    {
        if ( webappContext == null )
        {
            try
            {
                initServer();
            }
            catch ( Exception e )
            {
                throw new IllegalStateException( e );
            }
        }
        webappContext.getServletHandler().addServletWithMapping( new ServletHolder( servlet ), servlet.getPath() );
    }

    /**
     * @throws URISyntaxException
     */
    protected void initWebappContext( Server s )
        throws URISyntaxException
    {
        this.webappContext = new WebAppContext();
        webappContext.setContextPath( "/" );
        webappContext.setWar( "target" );
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers( new Handler[] { webappContext, new DefaultHandler() } );
        s.setHandler( handlers );
    }

    private String resourceFile( String resource )
        throws Exception
    {
        this.getClass().getClassLoader();
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL r = cl.getResource( resource );
        if ( r == null )
        {
            throw new IllegalStateException( "cannot find resource: " + resource );
        }
        if ( "file".equals( r.getProtocol() ) )
        {
            return new File( new URI( r.toExternalForm() ) ).getAbsolutePath();
        }
        else
        {
            InputStream in = null;
            FileOutputStream out = null;
            File target = FileUtil.createTempFile( "" );
            try
            {
                in = r.openStream();
                out = new FileOutputStream( target );
                int count = -1;
                byte[] buf = new byte[16000];
                while ( ( count = in.read( buf ) ) != -1 )
                {
                    out.write( buf, 0, count );
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
            return target.getAbsolutePath();
        }
    }

    /**
     * @return
     */
    protected Connector connector()
    {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost( host );
        if ( port != -1 )
        {
            connector.setPort( port );
        }
        return connector;
    }

    /**
     * @return
     */
    protected Connector sslConnector()
    {
        SslSocketConnector connector = new SslSocketConnector();
        String keystore;
        try
        {
            keystore = resourceFile( sslKeystore );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( e );
        }

        connector.setHost( host );
        if ( port != -1 )
        {
            connector.setPort( port );
        }
        connector.setKeystore( keystore );
        connector.setPassword( sslKeystorePassword );
        connector.setKeyPassword( sslKeystorePassword );

        return connector;
    }

    public void start()
        throws Exception
    {
        if ( server == null )
        {
            initServer();
        }
        server.start();

        int total = 0;
        while ( total < 3000 && !server.isStarted() )
        {
            server.wait( 10 );
            total += 10;
        }

        if ( !server.isStarted() )
        {
            throw new IllegalStateException( "Server didn't start in: " + total + "ms." );
        }

        port = server.getConnectors()[0].getLocalPort();
    }

    public void addBehaviour( String pathspec, Behaviour... behaviour )
    {
        logger.debug( "Adding " + behaviour.getClass().getSimpleName() + " to " + pathspec );
        addServlet( new BehaviourServlet( pathspec, behaviour ) );
    }

    public void stop()
        throws Exception
    {
        server.stop();

        int total = 0;
        while ( total < 3000 && server.isStarted() )
        {
            server.wait( 10 );
            total += 10;
        }

        if ( server.isStarted() )
        {
            throw new IllegalStateException( "Server didn't stop in: " + total + "ms." );
        }

    }

    public URL getUrl()
        throws MalformedURLException
    {
        String protocol;
        if ( ssl )
        {
            protocol = "https";
        }
        else
        {
            protocol = "http";
        }

        return new URL( protocol, host, port, "" );
    }

    public WebAppContext getWebappContext()
    {
        return webappContext;
    }

    public void setWebappContext( WebAppContext webappContext )
    {
        this.webappContext = webappContext;
    }

    public int getPort()
    {
        return port;
    }

    public void addFilter( String pathSpec, Filter filter )
    {
        if ( server == null )
        {
            try
            {
                initServer();
            }
            catch ( Exception e )
            {
                throw new IllegalStateException( e );
            }
        }

        String name = filter.toString();

        FilterMapping fm = new FilterMapping();
        fm.setPathSpec( pathSpec );
        fm.setFilterName( name );

        FilterHolder fh = new FilterHolder( filter );
        fh.setName( name );

        webappContext.getServletHandler().addFilter( fh, fm );
    }


}
