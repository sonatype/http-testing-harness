package org.sonatype.tests.redirect;

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

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.tests.async.util.AsyncSuiteConfiguration;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;
import org.sonatype.tests.jetty.server.behaviour.Pause;
import org.sonatype.tests.jetty.server.behaviour.Redirect;
import org.sonatype.tests.jetty.server.impl.JettyServerProvider;
import org.sonatype.tests.server.api.ServerProvider;

import com.ning.http.client.MaxRedirectException;
import com.ning.http.client.Response;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
public class RedirectHttpTest
    extends AsyncSuiteConfiguration
{

    @Test( expected = MaxRedirectException.class )
    public void testTooManyRedirects()
        throws Throwable
    {
        String url = url( "redirect", String.valueOf( client().getConfig().getMaxRedirects() + 1 ), "foo" );
        try
        {
            executeGet( url );
            fail( "expected error" );
        }
        catch ( ExecutionException e )
        {
            throw e.getCause();
        }
    }

    /**
     * Async Bug? Redirects == connection attempts? ("first connect is a redirection")
     * 
     * @throws Exception
     */
    @Test
    public void testMaxRedirects()
        throws Exception
    {
        String url = url( "redirect", String.valueOf( client().getConfig().getMaxRedirects() ), "foo" );
        Response response = executeGet( url );
        assertEquals( 200, response.getStatusCode() );
        assertEquals( "foo", response.getResponseBody() );
    }

    @Test
    public void testRedirectAbsolute()
        throws Exception
    {
        provider().addBehaviour( "/absolute/*", new Redirect( url( "content", "someContent" ) ) );
        String url = url( "absolute", "foo" );
        Response response = executeGet( url );
        assertEquals( 200, response.getStatusCode() );
        assertEquals( "someContent", response.getResponseBody() );
    }

    @Test
    public void testRedirectOtherServer()
        throws Exception
    {
        ServerProvider p = new JettyServerProvider();
        p.start();

        provider().addBehaviour( "/external/*", new Redirect( "http://localhost:" + p.getPort() + "/content/foo" ) );
        Response response = executeGet( url( "external", "bar" ) );

        assertEquals( 200, response.getStatusCode() );
        assertEquals( "foo", response.getResponseBody() );
    }

    @Test
    public void testRedirectToSSL()
        throws Exception
    {
        ServerProvider p = new JettyServerProvider();
        p.setSSL( "keystore", "password" );
        p.start();

        provider().addBehaviour( "/external/*", new Redirect( p.getUrl() + "/content/foo" ) );
        Response response = executeGet( url( "external", "bar" ) );

        assertEquals( 200, response.getStatusCode() );
        assertEquals( "foo", response.getResponseBody() );
    }

    @Test
    public void testTimeoutAfterRedirect()
        throws Exception
    {
        ServerProvider p = new JettyServerProvider();
        p.start();

        provider().addBehaviour( "/external/*", new Redirect( "http://localhost:" + p.getPort() + "/content/foo" ),
                                 new Pause( 10000 ) );
        Response response = executeGet( url( "external", "bar" ) );

        assertEquals( 200, response.getStatusCode() );
        assertEquals( "foo", response.getResponseBody() );
    }

}
