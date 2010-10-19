package org.sonatype.tests.jetty.server.behaviour;

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

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;
import org.sonatype.tests.server.api.Behaviour;

import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class ProxyAuth
    implements Behaviour
{
    private boolean authorized = false;

    private boolean challenged = false;

    private final String user;

    private final String password;

    public ProxyAuth( String user, String password )
    {
        this.user = user;
        this.password = password;
    }


    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        String headers = "";

        @SuppressWarnings( "unchecked" )
        Enumeration<String> names = request.getHeaderNames();
        while ( names.hasMoreElements() )
        {
            String name = names.nextElement();
            String value = request.getHeader( name );
            headers += name + ": " + value + "\n";
        }
        Log.debug( headers );
        String authHeader = request.getHeader( "Proxy-Authorization" );
        if ( authHeader == null )
        {
            response.addHeader( "Proxy-Authenticate", "BASIC realm=\"Test Proxy\"" );
            response.sendError( 407, "proxy auth required" );
            challenged = true;
            return false;
        }
        else
        {
            String expected = "BASIC " + new Base64Encoder().encode( ( user + ":" + password ).getBytes( "UTF-8" ) );
            this.authorized = expected.equals( authHeader );
        }
        return this.authorized;
    }

    public boolean isAuthorized()
    {
        return authorized;
    }

    public boolean isChallenged()
    {
        return challenged;
    }

    public void reset()
    {
        this.authorized = false;
        this.challenged = false;
    }

}
