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

import java.net.MalformedURLException;
import java.net.URL;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.sonatype.tests.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class JettyProxyProvider
    extends JettyServerProvider
    implements ServerProvider
{

    private ServerProvider real;

    private String password;

    private String user;

    public JettyProxyProvider( ServerProvider real )
        throws Exception
    {
        this.real = real;
    }

    public JettyProxyProvider( ServerProvider realServer, String user, String pw )
        throws Exception
    {
        this( realServer );
        this.user = user;
        this.password = pw;
    }

    @Override
    public void stop()
        throws Exception
    {
        real.stop();
        super.stop();
    }

    @Override
    public void start()
        throws Exception
    {
        real.start();
        super.start();
    }

    @Override
    public void initServer()
        throws Exception
    {
        server = new Server();

        Connector connector;
        if ( ssl )
        {
            connector = sslConnector();
        }
        else
        {
            connector = connector();
        }
        server.addConnector( connector );

        initWebappContext( server );

        addServlet( new ProxyTestServlet( user, password ) );

    }

    @Override
    public URL getUrl()
        throws MalformedURLException
    {
        return real.getUrl();
    }

    /* (non-Javadoc)
     * @see org.sonatype.tests.jetty.server.api.ServerProvider#addAuthentication(java.lang.String)
     */
    @Override
    public void addAuthentication( String pathSpec, String authName )
    {
    }

    /* (non-Javadoc)
     * @see org.sonatype.tests.jetty.server.api.ServerProvider#addUser(java.lang.String, java.lang.String)
     */
    @Override
    public void addUser( String user, String password )
    {
    }

    public ServerProvider getRealServer()
    {
        return real;
    }

    public void setRealServer( ServerProvider real )
    {
        this.real = real;
    }

}
