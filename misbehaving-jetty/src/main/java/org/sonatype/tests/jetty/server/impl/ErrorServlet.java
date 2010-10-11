package org.sonatype.tests.jetty.server.impl;

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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.server.api.TestServlet;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class ErrorServlet
    extends HttpServlet
    implements TestServlet
{

    public String getPath()
    {
        return "/error/*";
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        String path = req.getPathInfo().substring( 1 );
        String[] split = path.split( "/", 2 );
        int sc = Integer.valueOf( split[0] );
        String msg = split[1];
        resp.sendError( sc, msg );
    }

}