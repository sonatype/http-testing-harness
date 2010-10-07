package org.sonatype.tests.jetty.server.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.jetty.server.api.TestServlet;

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


/**
 * @author Benjamin Hanzelmann
 *
 */
public class ContentServlet
    extends HttpServlet
    implements TestServlet
{
    public String getPath()
    {
        return "/content/*";
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        // strip leading '/'
        String content = req.getPathInfo().substring( 1 );

        resp.setContentLength( content.length() );
        resp.setContentType( "text/plain" );
        resp.setCharacterEncoding( "UTF-8" );
        resp.setStatus( HttpServletResponse.SC_OK );
        resp.getWriter().write( content );
    }
}