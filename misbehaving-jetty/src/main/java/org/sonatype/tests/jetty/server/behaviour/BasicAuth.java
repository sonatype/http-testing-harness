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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.server.api.Behaviour;

import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class BasicAuth
    implements Behaviour
{

    private String password;

    private String user;

    private int failed = 0;

    public BasicAuth( String user, String password )
    {
        this.user = user;
        this.password = password;
    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        String userPass = new Base64Encoder().encode( ( user + ":" + password ).getBytes( "UTF-8" ) );
        if ( ( "Basic " + userPass ).equals( request.getHeader( "Authorization" ) ) )
        {
            return true;
        }

        failed++;

        response.sendError( 401, "not authorized" );
        return false;
    }

    public int getFailedCount()
    {
        return failed;
    }

}
