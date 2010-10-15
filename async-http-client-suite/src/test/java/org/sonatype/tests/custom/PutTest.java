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

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.tests.async.util.AsyncDebugHandler;
import org.sonatype.tests.async.util.AsyncSuiteConfiguration;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;
import org.sonatype.tests.jetty.runner.ConfigurationRunner.ConfiguratorList;
import org.sonatype.tests.jetty.server.behaviour.Consumer;
import org.sonatype.tests.jetty.server.behaviour.ErrorBehaviour;
import org.sonatype.tests.jetty.server.util.FileUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Response;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
@ConfiguratorList( { "DefaultSuiteConfigurator.list", "AuthSuiteConfigurator.list" } )
public class PutTest
    extends AsyncSuiteConfiguration
{

    @Override
    public void before()
        throws Exception
    {
        super.before();
        setAuthentication( "user", "password", false );
    }

    @Test
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
            largeFile = FileUtil.createTempFile( bytes, (int) repeats );

            Builder cfg = new Builder();
            cfg.setIdleConnectionTimeoutInMs( (int) heapSize );
            cfg.setConnectionTimeoutInMs( (int) heapSize );
            cfg.setRequestTimeoutInMs( (int) heapSize );
            AsyncHttpClient c = new AsyncHttpClient( cfg.build() );

            BoundRequestBuilder put = c.preparePut( url );
            requestSettings( put );
            put.setBody( largeFile );
            put.execute( new AsyncDebugHandler() ).get();

            assertEquals( largeFile.length(), consumer.getTotal() );
        }
        finally
        {
            FileUtil.delete( largeFile );
        }

    }

    @Test
    public void testPutMethodNotSupported()
        throws Exception
    {
        provider().addBehaviour( "/methodnotsupported/*", new ErrorBehaviour( 501, "errormsg" )
        {

            @Override
            public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
                throws Exception
            {
                response.setHeader( "Allow", "GET, HEAD, POST" );
                return super.execute( request, response, ctx );
            }

        } );

        String url = url( "methodnotsupport", "error" );
        BoundRequestBuilder rb = client().preparePut( url );
        Response response = execute( rb );

        assertEquals( 405, response.getStatusCode() );
    }

    @Test
    public void testPutError()
        throws Exception
    {
        provider().addBehaviour( "/methodsupported/*", new ErrorBehaviour( 404, "errormsg" )
        {

            @Override
            public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
                throws Exception
            {
                response.setHeader( "Allow", "GET, HEAD, POST, PUT" );
                return super.execute( request, response, ctx );
            }

        } );
        String url = url( "methodsupported", "404", "errormsg" );
        BoundRequestBuilder rb = client().preparePut( url );
        Response response = execute( rb );

        assertEquals( 404, response.getStatusCode() );
        assertEquals( "errormsg", response.getStatusText() );
    }


}
