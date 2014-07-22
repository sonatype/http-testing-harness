/*
 * Copyright (c) 2010-2014 Sonatype, Inc. All rights reserved.
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
package org.sonatype.tests.http.server.jetty.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.tests.http.server.jetty.behaviour.Consumer;
import org.sonatype.tests.http.server.jetty.behaviour.Content;
import org.sonatype.tests.http.server.jetty.behaviour.Debug;

/**
 * @author Benjamin Hanzelmann
 */
public class ProxyServerTest
{
    @Before
    public void clearJvmHttpAuthCaches()
    {
        // FIXME: this is circumvention got from here: http://stackoverflow.com/questions/480895/reset-the-authenticator-credentials
        // Actual Java bug is this: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6626700
        sun.net.www.protocol.http.AuthCacheValue.setAuthCache( new sun.net.www.protocol.http.AuthCacheImpl() );
    }

    @Test
    public void testProxyGet()
        throws Exception
    {
        JettyProxyProvider proxy = new JettyProxyProvider();
        proxy.addBehaviour( "/*", new Debug(), new Content() );
        proxy.start();

        URL url = new URL( "http://speutel.invalid/foo" );
        SocketAddress sa = new InetSocketAddress( "localhost", proxy.getPort() );
        Proxy p = new Proxy( Type.HTTP, sa );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection( p );

        conn.setDoInput( true );
        conn.connect();
        assertEquals( 200, conn.getResponseCode() );
        assertEquals( "foo", read( conn.getContent() ).trim() );
        conn.disconnect();
    }

    private String read( Object o )
        throws IOException
    {
        if ( o instanceof InputStream )
        {
            return read( (InputStream) o );
        }
        else
        {
            return o.toString();
        }
    }

    private String read( InputStream in )
        throws IOException
    {
        BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
        StringBuilder builder = new StringBuilder();
        String line;
        while ( ( line = r.readLine() ) != null )
        {
            builder.append( line ).append( "\n" );
        }

        return builder.toString();
    }

    @Test
    public void testProxyAuthGet()
        throws Exception
    {
        JettyProxyProvider proxy = new JettyProxyProvider( "u", "p" );
        proxy.addBehaviour( "/*", new Debug(), new Content() );
        proxy.start();

        URL url = new URL( "http://speutel.invalid/foo" );
        SocketAddress sa = new InetSocketAddress( "localhost", proxy.getPort() );
        Proxy p = new Proxy( Type.HTTP, sa );

        Authenticator.setDefault( new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                if ( getRequestingHost().equals( "localhost" ) )
                {
                    String password = "p";
                    return new PasswordAuthentication( "u", password.toCharArray() );
                }
                return super.getPasswordAuthentication();
            }
        } );

        HttpURLConnection conn = (HttpURLConnection) url.openConnection( p );

        conn.setDoInput( true );
        conn.connect();
        assertEquals( 200, conn.getResponseCode() );
        assertEquals( "foo", read( conn.getContent() ).trim() );
        conn.disconnect();
    }

    @Test
    public void testProxyAuthGetFail407()
        throws Exception
    {
        JettyProxyProvider proxy = new JettyProxyProvider( "u", "p" );
        proxy.addBehaviour( "/*", new Debug(), new Content() );
        proxy.start();

        URL url = new URL( "http://speutel.invalid/foo" );
        SocketAddress sa = new InetSocketAddress( "localhost", proxy.getPort() );
        Proxy p = new Proxy( Type.HTTP, sa );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection( p );

        try
        {
            conn.setDoInput( true );
            conn.connect();
            conn.getResponseCode();
        }
        catch ( IOException e )
        {
            assertTrue( "expected status code 407", e.getMessage().contains( "407" ) );
        }
        finally
        {
            conn.disconnect();
        }
    }

    @Test
    public void testAuthAfterProxyAuthGetFail401()
        throws Exception
    {
        JettyProxyProvider proxy = new JettyProxyProvider( "u", "p" );
        proxy.addBehaviour( "/*", new Debug(), new Content() );
        proxy.addAuthentication( "/*", "BASIC" );
        proxy.addUser( "user", "password" );
        proxy.start();
        Authenticator.setDefault( new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                if ( getRequestingHost().equals( "localhost" ) )
                {
                    String password = "p";
                    return new PasswordAuthentication( "u", password.toCharArray() );
                }
                return super.getPasswordAuthentication();
            }
        } );

        URL url = new URL( "http://speutel.invalid/foo" );
        SocketAddress sa = new InetSocketAddress( "localhost", proxy.getPort() );
        Proxy p = new Proxy( Type.HTTP, sa );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection( p );

        try
        {
            conn.setDoInput( true );
            conn.connect();
            assertEquals( 401, conn.getResponseCode() );
        }
        finally
        {
            conn.disconnect();
        }

    }

    @Test
    public void testAuthAfterProxyAuthGet()
        throws Exception
    {
        JettyProxyProvider proxy = new JettyProxyProvider( "u", "p" );
        proxy.addBehaviour( "/*", new Debug(), new Content() );
        proxy.addAuthentication( "/*", "BASIC" );
        proxy.addUser( "user", "password" );
        proxy.start();
        Authenticator.setDefault( new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                if ( getRequestingHost().equals( "localhost" ) )
                {
                    String password = "p";
                    return new PasswordAuthentication( "u", password.toCharArray() );
                }
                else
                {
                    String password = "password";
                    return new PasswordAuthentication( "user", password.toCharArray() );

                }
            }
        } );

        URL url = new URL( "http://speutel.invalid/foo" );
        SocketAddress sa = new InetSocketAddress( "localhost", proxy.getPort() );
        Proxy p = new Proxy( Type.HTTP, sa );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection( p );

        try
        {
            conn.setDoInput( true );
            conn.connect();
            assertEquals( 200, conn.getResponseCode() );
        }
        finally
        {
            conn.disconnect();
        }
    }

    @Test
    public void testProxyPut()
        throws Exception
    {
        JettyProxyProvider proxy = new JettyProxyProvider();
        Consumer consumer = new Consumer();
        proxy.addBehaviour( "/*", new Debug(), consumer );
        proxy.start();

        URL url = new URL( "http://speutel.invalid/foo" );
        SocketAddress sa = new InetSocketAddress( "localhost", proxy.getPort() );
        Proxy p = new Proxy( Type.HTTP, sa );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection( p );

        conn.setDoOutput( true );
        conn.setRequestMethod( "PUT" );
        conn.connect();
        byte[] bytes = "TestPut".getBytes( "US-ASCII" );
        conn.getOutputStream().write( bytes );
        conn.getOutputStream().close();
        assertEquals( 200, conn.getResponseCode() );
        assertEquals( bytes.length, consumer.getTotal() );
        conn.disconnect();
    }

}
