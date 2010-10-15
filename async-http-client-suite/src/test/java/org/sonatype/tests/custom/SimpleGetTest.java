package org.sonatype.tests.custom;

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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.tests.async.util.AssertingAsyncHandler;
import org.sonatype.tests.async.util.AsyncSuiteConfiguration;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;

import com.ning.http.client.Response;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
public class SimpleGetTest
    extends AsyncSuiteConfiguration
{

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
        String url = url( "error", "500/errormsg" );
        Response response = executeGet( url );
        int code = response.getStatusCode();
        String text = response.getStatusText();

        assertEquals( 500, code );
        assertEquals( "errormsg", text );
    }

    @Test
    public void testPause()
        throws Exception
    {
        String url = url( "pause", "1550", "1", "2", "3" );

        long begin = System.currentTimeMillis();
        Response response = executeGet( url );
        long end = System.currentTimeMillis();

        int code = response.getStatusCode();
        String text = response.getStatusText();
        String body = response.getResponseBody();

        assertEquals( 200, code );
        assertEquals( "OK", text );
        assertEquals( "1550/1/2/3", body );
        assertTrue( "real delta: " + ( end - begin ), end - begin >= 1450 );

    }

    @Test
    public void testStutter()
        throws Exception
    {
        String url = url( "stutter", "520", "1", "2", "3" );

        long begin = System.currentTimeMillis();
        Response response = executeGet( url );
        long end = System.currentTimeMillis();

        int code = response.getStatusCode();
        String text = response.getStatusText();
        String body = response.getResponseBody();

        assertEquals( 200, code );
        assertEquals( "OK", text );
        assertEquals( "123", body );
        assertTrue( "real delta: " + ( end - begin ), end - begin >= 1450 );
    }

    /**
     * Fails for Authentication needed...
     * 
     * @throws Exception
     */
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

    /**
     * Fails for authenticated.
     * 
     * @throws Exception
     */
    @Test
    public void testRedirect()
        throws Exception
    {
        String url = url( "redirect", "3", "content" );

        Response response = executeGet( url );
        assertEquals( "content", response.getResponseBody() );
    }

    @Test
    public void testReadFullyUnspecifiedLength()
    {
        fail( "unimplemented" );
    }

    private String contentUrl( String suffix )
    {
        String path = "content";
        return url( path, suffix );
    }
}
