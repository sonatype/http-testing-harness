package org.sonatype.tests.http.server.jetty.impl;

/*
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.log.Log;
import org.sonatype.tests.http.server.api.TestServlet;

/**
 * @author Benjamin Hanzelmann
 */
public class ProxyTestServlet
    extends ProxyServlet
    implements TestServlet
{

    private String password = null;

    private String principal = null;

    public String getPath()
    {
        return "/*";
    }

    public ProxyTestServlet( String principal, String password )
    {
        super( "localhost" );
        this.principal = principal;
        this.password = password;
    }

    @Override
    public void service( ServletRequest request, ServletResponse res )
        throws ServletException, IOException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        Log.debug( req.getPathInfo() );
        if ( principal != null )
        {
            String header = req.getHeader( "Proxy-Authorization" );
            HttpServletResponse response = (HttpServletResponse) res;
            if ( header == null )
            {
                response.setStatus( HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED );
                response.addHeader( "Proxy-Authenticate", "Basic realm=\"Test Server\"" );
                response.sendError( HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED );
                return;
            }
            else
            {
                String data = header.substring( "BASIC ".length() );
                data = new String( B64Code.decode( data ) );
                Log.debug( data );
                String[] creds = data.split( ":" );

                if ( !creds[0].equals( principal ) || !creds[1].equals( password ) )
                {
                    response.sendError( HttpServletResponse.SC_UNAUTHORIZED );
                }
            }
        }

        super.service( request, res );
    }


}
