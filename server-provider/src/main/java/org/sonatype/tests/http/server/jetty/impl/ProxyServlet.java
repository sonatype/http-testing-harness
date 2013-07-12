/*
 * Copyright (c) 2010-2013 Sonatype, Inc. All rights reserved.
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

//Based on ProxyServlet.java
//Copyright 2004-2004 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author gregw
 */
public class ProxyServlet
    implements Servlet
{
    protected Set<String> skipHeaders = new HashSet<String>();
    {
        skipHeaders.add( "proxy-connection" );
        skipHeaders.add( "connection" );
        skipHeaders.add( "keep-alive" );
        skipHeaders.add( "transfer-encoding" );
        skipHeaders.add( "te" );
        skipHeaders.add( "trailer" );
        skipHeaders.add( "proxy-authorization" );
        skipHeaders.add( "proxy-authenticate" );
        skipHeaders.add( "upgrade" );
    }


    private ServletConfig config;

    private ServletContext context;

    private String host = null;

    public ProxyServlet()
    {
    }

    public ProxyServlet( String realHost )
    {
        this.host = realHost;
    }

    public void init( ServletConfig config )
        throws ServletException
    {
        this.config = config;
        this.context = config.getServletContext();
    }

    public ServletConfig getServletConfig()
    {
        return config;
    }

    public void service( ServletRequest req, ServletResponse res )
        throws ServletException, IOException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if ( "CONNECT".equalsIgnoreCase( request.getMethod() ) )
        {
            handleConnect( request, response );
        }
        else
        {
            String uri = request.getRequestURI();
            if ( request.getQueryString() != null )
            {
                uri += "?" + request.getQueryString();
            }
            URL url =
                new URL( request.getScheme(), host != null ? host : request.getServerName(), request.getServerPort(),
                         uri );

            context.log( "URL=" + url );

            URLConnection connection = url.openConnection();
            connection.setAllowUserInteraction( false );

            // Set method
            HttpURLConnection http = null;
            if ( connection instanceof HttpURLConnection )
            {
                http = (HttpURLConnection) connection;
                http.setRequestMethod( request.getMethod() );
                http.setInstanceFollowRedirects( false );
            }

            // check connection header
            String connectionHdr = request.getHeader( "Connection" );
            if ( connectionHdr != null )
            {
                connectionHdr = connectionHdr.toLowerCase( Locale.ENGLISH );
                if ( connectionHdr.equals( "keep-alive" ) || connectionHdr.equals( "close" ) )
                {
                    connectionHdr = null;
                }
            }

            // copy headers
            boolean xForwardedFor = false;
            boolean hasContent = false;

            @SuppressWarnings( "rawtypes" )
            Enumeration enm = request.getHeaderNames();
            while ( enm.hasMoreElements() )
            {
                String hdr = (String) enm.nextElement();
                String lhdr = hdr.toLowerCase();

                if ( skipHeaders.contains( lhdr ) )
                {
                    continue;
                }
                if ( connectionHdr != null && connectionHdr.indexOf( lhdr ) >= 0 )
                {
                    continue;
                }

                if ( "content-type".equals( lhdr ) )
                {
                    hasContent = true;
                }

                @SuppressWarnings( "rawtypes" )
                Enumeration vals = request.getHeaders( hdr );
                while ( vals.hasMoreElements() )
                {
                    String val = (String) vals.nextElement();
                    if ( val != null )
                    {
                        connection.addRequestProperty( hdr, val );
                        context.log( "req " + hdr + ": " + val );
                        xForwardedFor |= "X-Forwarded-For".equalsIgnoreCase( hdr );
                    }
                }
            }

            connection.setRequestProperty( "Via", "1.1 (jetty)" );
            if ( !xForwardedFor )
            {
                connection.addRequestProperty( "X-Forwarded-For", request.getRemoteAddr() );
            }

            connection.setUseCaches( false );

            // customize Connection

            try
            {
                connection.setDoInput( true );

                InputStream in = request.getInputStream();
                if ( hasContent )
                {
                    connection.setDoOutput( true );
                    copy( in, connection.getOutputStream() );
                }

                connection.connect();
            }
            catch ( Exception e )
            {
                context.log( "proxy", e );
            }

            InputStream proxy_in = null;

            int code = 500;
            if ( http != null )
            {
                proxy_in = http.getErrorStream();

                code = http.getResponseCode();
                response.setStatus( code );
            }

            if ( proxy_in == null )
            {
                try
                {
                    proxy_in = connection.getInputStream();
                }
                catch ( Exception e )
                {
                    context.log( "stream", e );
                    proxy_in = http.getErrorStream();
                }
            }

            response.setHeader( "Date", null );
            response.setHeader( "Server", null );

            int h = 0;
            String hdr = connection.getHeaderFieldKey( h );
            String val = connection.getHeaderField( h );
            while ( hdr != null || val != null )
            {
                String lhdr = hdr != null ? hdr.toLowerCase() : null;
                if ( hdr != null && val != null && !skipHeaders.contains( lhdr ) )
                {
                    response.addHeader( hdr, val );
                }

                context.log( "res " + hdr + ": " + val );

                h++;
                hdr = connection.getHeaderFieldKey( h );
                val = connection.getHeaderField( h );
            }
            response.addHeader( "Via", "1.1 (jetty)" );

            // Handle
            if ( proxy_in != null )
            {
                copy( proxy_in, response.getOutputStream() );
            }

        }
    }

    public void handleConnect( HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        String uri = request.getRequestURI();

        context.log( "CONNECT: " + uri );
        {

            URI realURI = URI.create( uri );
            Socket socket = new Socket( realURI.getHost(), realURI.getPort() );

            response.setStatus( 200 );
            response.setHeader( "Connection", "close" );
            response.flushBuffer();

            System.err.println( response );

            ServletOutputStream toClient = null;
            InputStream fromServer = null;
            ServletInputStream fromClient = null;
            OutputStream toServer = null;
            try
            {
                fromServer = socket.getInputStream();
                toClient = response.getOutputStream();
                copy( fromServer, toClient );

                fromClient = request.getInputStream();
                toServer = socket.getOutputStream();
                copy( fromClient, toServer );
            }
            finally
            {
                close( toServer );
                close( fromServer );
                close( toClient );
                close( fromClient );
            }
        }
    }

    private void close( Closeable closable )
        throws IOException
    {
        if ( closable != null )
        {
            closable.close();
        }
    }

    private void copy( InputStream in, OutputStream out )
        throws IOException
    {
        int count = -1;
        byte[] b = new byte[16 * 1024];

        while ( ( count = in.read( b ) ) != -1 )
        {
            out.write( b, 0, count );
        }
    }

    public String getServletInfo()
    {
        return "Test Proxy Servlet";
    }

    public void destroy()
    {

    }
}
