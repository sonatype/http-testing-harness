package org.sonatype.tests.jetty.server.suites;

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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.tests.jetty.server.api.SuiteConfiguration;
import org.sonatype.tests.jetty.server.api.SuiteConfigurator;
import org.sonatype.tests.jetty.server.behaviour.Consumer;
import org.sonatype.tests.jetty.server.util.AssertingAsyncHandler;
import org.sonatype.tests.jetty.server.util.AsyncDebugHandler;
import org.sonatype.tests.jetty.server.util.FileUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Response;

/**
 * @author Benjamin Hanzelmann
 */
public class SimpleTestSuite
    extends SuiteConfiguration
{


    public SimpleTestSuite( SuiteConfigurator configurator )
    {
        super( configurator );
    }

    @Override
    @Before
    public void before()
        throws Exception
    {
        super.before();
    }

    @Test
    public void testSuccessful()
        throws Exception
    {
        String content = "someContent";
        String url = contentUrl( content );
        Response response = executeGet( url );
        String body = response.getResponseBody();

        assertEquals( content, body );
    }

    @Test
    public void testError()
        throws Exception
    {
        String url = url( "error", "404/errormsg" );
        Response response = executeGet( url );
        int code = response.getStatusCode();
        String text = response.getStatusText();

        assertEquals( 404, code );
        assertEquals( "errormsg", text );
    }

    @Test
    public void testPause()
        throws Exception
    {
        String url = url( "pause", "1500", "1", "2", "3" );

        long begin = System.currentTimeMillis();
        Response response = executeGet( url );
        long end = System.currentTimeMillis();

        int code = response.getStatusCode();
        String text = response.getStatusText();
        String body = response.getResponseBody();

        assertEquals( 200, code );
        assertEquals( "OK", text );
        assertEquals( "1500/1/2/3", body );
        assertTrue( "real delta: " + ( end - begin ), end - begin >= 1500 );

    }

    @Test
    public void testStutter()
        throws Exception
    {
        String url = url( "stutter", "500", "1", "2", "3" );

        long begin = System.currentTimeMillis();
        Response response = executeGet( url );
        long end = System.currentTimeMillis();

        int code = response.getStatusCode();
        String text = response.getStatusText();
        String body = response.getResponseBody();

        assertEquals( 200, code );
        assertEquals( "OK", text );
        assertEquals( "123", body );
        assertTrue( "real delta: " + ( end - begin ), end - begin >= 1500 );
    }

    @Test
    public void testTruncate()
        throws Exception
    {
        String url = url( "truncate", "5", "first", "second" );

        AssertingAsyncHandler handler = new AssertingAsyncHandler();
        handler.addBodyParts( "first" );
        handler.setExpectedThrowables( new IOException(), new TimeoutException() );

        try
        {
            Response response = executeGet( url );

            assertEquals( "", response.getResponseBody() );
            fail( "expected error" );
        }
        catch ( Exception e )
        {
            if ( handler.getAssertionError() != null )
            {
                throw handler.getAssertionError();
            }
        }
    }

    @Test
    public void testRedirect()
        throws Exception
    {
        String url = url( "redirect", "3", "content" );

        Response response = executeGet( url );
        assertEquals( "content", response.getResponseBody() );
    }

    @Test
    @Ignore
    public void testPutLargeFile()
        throws Exception
    {
        File largeFile = null;
        try
        {
            Consumer consumer = new Consumer();
            provider().addBehaviour( "/consume/*", consumer );
            
            String url = url( "consume", "foo" );
            byte[] bytes = "RatherLargeFile".getBytes( "UTF-16" );
            long heapSize = Runtime.getRuntime().maxMemory();
            long repeats = ( heapSize / bytes.length ) + 1;
            logger.debug( "creating file of size ~" + heapSize );
            largeFile = FileUtil.createTempFile( bytes, repeats );
            logger.debug( "created file of size " + largeFile.length() );
            
            Builder cfg = new Builder();
            cfg.setIdleConnectionTimeoutInMs( (int) heapSize );
            cfg.setConnectionTimeoutInMs( (int) heapSize );
            cfg.setRequestTimeoutInMs( (int) heapSize );
            AsyncHttpClient c = new AsyncHttpClient( cfg.build() );
            
            BoundRequestBuilder put = c.preparePut( url );
            settings( put );
            put.setBody( largeFile );
            put.execute( new AsyncDebugHandler() ).get();

            assertEquals( largeFile.length(), consumer.getTotal() );
        }
        finally
        {
            FileUtil.delete( largeFile );
        }

    }

    private String contentUrl( String suffix )
    {
        String path = "content";
        return url( path, suffix );
    }
}
