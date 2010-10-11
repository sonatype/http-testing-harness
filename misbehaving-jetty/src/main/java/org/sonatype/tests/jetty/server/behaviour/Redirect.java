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

import static org.sonatype.tests.jetty.server.behaviour.BehaviourHelper.*;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 */
public class Redirect
    implements Behaviour
{

    private int count = -1;

    private int redirectCount = 0;

    private String content;

    private String target;

    public Redirect()
    {
        super();
    }

    public Redirect( int count )
    {
        this.count = count;
    }

    /**
     * @param url
     */
    public Redirect( String url )
    {
        this.target = url;
    }

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( target != null )
        {
            return;
        }
        if ( count == -1 )
        {
            count = Integer.valueOf( firstPart( request.getPathInfo() ) );
            content = content( request.getPathInfo() );
            System.err.println( "saving content: " + content );
        }
        else if ( content == null )
        {
            content = request.getPathInfo();
        }

    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( target != null )
        {
            response.setContentLength( 0 );
            response.sendRedirect( target );
            return false;
        }
        if ( redirectCount < count )
        {
            System.err.println( "Redirecting... " + redirectCount );
            response.setContentLength( 0 );
            response.sendRedirect( String.valueOf( ++redirectCount ) );
            return false;
        }
        System.err.println( "setting saved content: " + content );
        setContent( content, ctx );

        count = -1;
        redirectCount = 0;
        content = null;

        return true;
    }


}
