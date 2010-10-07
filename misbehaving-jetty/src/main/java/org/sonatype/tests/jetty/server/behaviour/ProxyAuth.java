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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.tests.jetty.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class ProxyAuth
    implements Behaviour
{
    private Logger logger = LoggerFactory.getLogger( ProxyAuth.class );

    private boolean authorized = false;

    private boolean challenged = false;

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {

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
        logger.debug( headers );
        if ( request.getHeader( "Proxy-Authorization" ) == null )
        {
            response.addHeader( "Proxy-Authenticate", "BASIC realm" );
            response.sendError( 407, "proxy auth required" );
            challenged = true;
            return false;
        }
        this.authorized = true;
        return true;
    }

    public boolean isAuthorized()
    {
        return authorized;
    }

    public boolean isChallenged()
    {
        return challenged;
    }

}
