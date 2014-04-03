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
package org.sonatype.tests.http.server.jetty.behaviour;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.sonatype.tests.http.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class Debug
    implements Behaviour
{

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        Log.debug( "context path " + request.getContextPath() );
        Log.debug( "path info " + request.getPathInfo() );

        @SuppressWarnings( "rawtypes" )
        Enumeration headerNames = request.getHeaderNames();
        while ( headerNames.hasMoreElements() )
        {
            String element = headerNames.nextElement().toString();
            Log.debug( element + ": " + request.getHeader( element ) );
        }

        return true;
    }

}
