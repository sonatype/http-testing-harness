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

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.junit.Assert;
import org.sonatype.tests.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 */
public class Consumer
    implements Behaviour
{

    private int total;

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        int length = request.getContentLength();
        Log.debug( "Consumer#execute: " + length );

        ServletInputStream in = request.getInputStream();
        int count;
        byte[] b = new byte[16000];
        while ( ( count = in.read( b ) ) != -1 )
        {
            total += count;
        }

        Assert.assertEquals( "announced length does not match", length, total );

        return true;
    }

    public int getTotal()
    {
        int ret = total;
        total = 0;
        return ret;
    }

}
