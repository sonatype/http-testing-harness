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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.tests.jetty.server.behaviour.Content;
import org.sonatype.tests.jetty.server.behaviour.Pause;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class JettyServerProviderTest
{
    
    private static JettyServerProvider provider;

    @BeforeClass
    public static void init()
        throws Exception
    {
        provider = new JettyServerProvider();
        provider.addDefaultServices();
        provider.start();
    }

    @AfterClass
    public static void afterClass()
        throws Exception
    {
        provider.stop();
    }

    @Test
    public void testConnect()
        throws Exception
    {
        Socket s = new Socket();
        s.connect( new InetSocketAddress( "localhost", provider.getPort() ) );
        s.close();
    }

    @Test
    public void testContent()
        throws Exception
    {
        String content = "someContent";
        URL url = new URL( "http://localhost:" + provider.getPort() + "/content/" + content );
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
        String line = r.readLine();
        r.close();
        assertEquals( content, line );
    }

    @Test
    public void testError()
        throws Exception
    {
        URL url = new URL( "http://localhost:" + provider.getPort() + "/error/404/errormsg" );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        assertEquals(404, conn.getResponseCode());
        assertEquals( "errormsg", conn.getResponseMessage() );
    }

    @Test
    public void testBehaviour()
        throws Exception
    {
        provider.addBehaviour( "behave", new Pause( 550 ), new Content() );

        long begin = System.currentTimeMillis();
        URL url = new URL( "http://localhost:" + provider.getPort() + "/behave/baby" );
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
        String line = r.readLine();
        long end = System.currentTimeMillis();
        assertEquals( "Received content was not correct.", "baby", line );
        assertTrue( "expected 500ms, real delta: " + ( end - begin ), end - begin >= 500 );
    }

    @Test
    public void testStutter()
        throws Exception
    {
        byte[] buffer = new byte[5];
        int read = -1;
        
        URL url = new URL( "http://localhost:" + provider.getPort() + "/stutter/250/one/two/three" );
        URLConnection conn = url.openConnection();
        long begin = System.currentTimeMillis();
        InputStream in = conn.getInputStream();

        read = in.read( buffer );
        long end = System.currentTimeMillis();

        String value = new String( buffer, 0, read, "UTF-8" );
        assertEquals( "one", value );
        assertTrue( "expected 200ms, real delta: " + ( end - begin ), end - begin >= 200 );
        assertEquals( 3, read );

        begin = System.currentTimeMillis();
        read = in.read( buffer );
        end = System.currentTimeMillis();
        value = new String( buffer, 0, read, "UTF-8" );
        assertEquals( "two", value );
        assertTrue( "expected 200ms, real delta: " + ( end - begin ), end - begin >= 200 );
        assertEquals( 3, read );

        begin = System.currentTimeMillis();
        read = in.read( buffer );
        end = System.currentTimeMillis();
        value = new String( buffer, 0, read, "UTF-8" );

        assertEquals( "three", value );
        assertTrue( "expected 200ms, real delta: " + ( end - begin ), end - begin >= 200 );
        assertEquals( 5, read );
    }

    @Test
    public void testPause()
        throws Exception
    {
        URL url = new URL( "http://localhost:" + provider.getPort() + "/pause/550/content" );
        URLConnection conn = url.openConnection();

        long begin = System.currentTimeMillis();
        InputStream in = conn.getInputStream();
        BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
        String line = r.readLine();
        long end = System.currentTimeMillis();
        r.close();
        assertEquals( "550/content", line );
        assertTrue( "expected 500ms, real delta: " + ( end - begin ), end - begin >= 500 );
    }

    @Test
    public void testTruncate()
        throws Exception
    {
        URL url = new URL( "http://localhost:" + provider.getPort() + "/truncate/5/content" );
        URLConnection conn = url.openConnection();

        InputStream in = conn.getInputStream();
        BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
        String line = r.readLine();
        r.close();
        assertEquals( "conte", line );
    }

    @Test
    public void testTimeout()
        throws Exception
    {
        URL url = new URL( "http://localhost:" + provider.getPort() + "/timeout/550/content" );

        long begin = System.currentTimeMillis();
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
        String line = r.readLine();
        long end = System.currentTimeMillis();
        r.close();
        assertTrue( "real delta: " + ( end - begin ), end - begin >= 500 );
        assertEquals( null, line );

    }
}