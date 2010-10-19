package org.sonatype.tests.general.proxy;

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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.tests.async.util.AsyncSuiteConfiguration;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;
import org.sonatype.tests.jetty.runner.ConfigurationRunner.Configurators;
import org.sonatype.tests.jetty.server.configurations.HttpProxyConfigurator;
import org.sonatype.tests.jetty.server.impl.JettyProxyProvider;
import org.sonatype.tests.jetty.server.impl.JettyServerProvider;

import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;
import com.ning.http.client.Response;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
@Configurators( HttpProxyConfigurator.class )
public class SimpleHttpProxyTest
    extends AsyncSuiteConfiguration
{

    protected JettyServerProvider realServer;

    @Override
    @Before
    public void before()
        throws Exception
    {
        super.before();
        realServer = (JettyServerProvider) ( (JettyProxyProvider) provider() ).getRealServer();
    }

    @Override
    protected Builder settings( Builder rb )
    {
        return super.settings( rb ).setProxyServer( new ProxyServer( "localhost", provider().getPort() ) );
    }

    @Test
    public void testGet()
        throws Exception
    {
        String url = url( "content", "something" );
        BoundRequestBuilder get = client().prepareGet( url );
        Response response = execute( get );
        System.err.println( response.getHeaders() );
        assertEquals( 200, response.getStatusCode() );
        assertEquals( "something", response.getResponseBody() );
    }

    @Test
    public void testHead()
        throws Exception
    {
        String url = url( "content", "something" );
        BoundRequestBuilder get = client().prepareHead( url );
        Response response = execute( get );
        assertEquals( "", response.getResponseBody() );
        assertEquals( 200, response.getStatusCode() );
    }

    @Test
    public void testBasicAuthBehindProxy()
        throws Exception
    {
        authServer( "BASIC" );

        ( (JettyProxyProvider) provider() ).setRealServer( realServer );

        setAuthentication( "u", "p", false );
        BoundRequestBuilder rb = client().prepareGet( url( "content", "something" ) );
        Response response = execute( rb );

        assertEquals( "something", response.getResponseBody() );
    }

    @Test
    public void testDigestAuthBehindProxy()
        throws Exception
    {
        authServer( "DIGEST" );

        ( (JettyProxyProvider) provider() ).setRealServer( realServer );

        BoundRequestBuilder rb = client().prepareGet( url( "content", "something" ) );
        rb.setRealm( new Realm.RealmBuilder().setPrincipal( "u" ).setPassword( "p" ).setUsePreemptiveAuth( false ).build() );
        Response response = execute( rb );

        assertEquals( "something", response.getResponseBody() );
    }

    /**
     * FIXME investigate failure
     */
    @Test
    @Ignore( "Fails after changes for eclipse-commons" )
    public void testBasicAuthFailBehindProxy()
        throws Exception
    {
        authServer( "BASIC" );

        JettyProxyProvider proxy = (JettyProxyProvider) provider();
        proxy.setRealServer( realServer );

        setAuthentication( "u", "wrong", false );
        BoundRequestBuilder rb = client().prepareGet( url( "content", "something" ) );
        Response response = execute( rb );

        System.err.println( response.getResponseBody() );

        assertEquals( 401, response.getStatusCode() );
    }

    /**
     * FIXME investigate failure
     */
    @Test
    @Ignore( "Fails after changes for eclipse-commons" )
    public void testDigestAuthFailBehindProxy()
        throws Exception
    {
        authServer( "DIGEST" );

        ( (JettyProxyProvider) provider() ).setRealServer( realServer );

        BoundRequestBuilder rb = client().prepareGet( url( "content", "something" ) );
        rb.setRealm( new Realm.RealmBuilder().setPrincipal( "u" ).setPassword( "wrong" ).setUsePreemptiveAuth( false ).build() );
        Response response = execute( rb );

        assertEquals( 401, response.getStatusCode() );
    }

    private void authServer( String method )
        throws Exception
    {
        realServer.stop();
        realServer.initServer();
        realServer.addDefaultServices();
        realServer.addAuthentication( "/*", method );
        realServer.addUser( "u", "p" );
        realServer.start();
    }
}
