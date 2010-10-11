package org.sonatype.tests.async.connector.behaviour;

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

import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.tests.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class Upload
    implements Behaviour
{

    private static Logger logger = LoggerFactory.getLogger( Upload.class );

    private byte[] content;

    private byte[] seen;

    private String path;

    public Upload( byte[] expectedContent )
    {
        super();
        this.content = expectedContent;
    }

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {

    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {

        path = request.getPathInfo();
        logger.debug( "upload: " + path + ", " + request.getContentLength() );
        ServletInputStream in = request.getInputStream();

        int count;
        ByteArrayOutputStream out = new ByteArrayOutputStream( content.length );
        byte[] b = new byte[16000];
        while ( ( count = in.read( b ) ) != -1 )
        {
            out.write( b, 0, count );
        }

        seen = out.toByteArray();

        out.close();

        return false;
    }

    public byte[] seenBytes()
    {
        return seen;
    }

    public void assertContent()
    {
        assertArrayEquals( "assertion failed for " + path, content, seen );
    }

}
