/*
 * Copyright (c) 2010-2014 Sonatype, Inc. All rights reserved.
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

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.tests.http.runner.annotations.Configurators;
import org.sonatype.tests.http.runner.junit.ConfigurationRunner;
import org.sonatype.tests.http.server.jetty.behaviour.Consumer;
import org.sonatype.tests.http.server.jetty.configurations.DefaultSuiteConfigurator;

/**
 * @author Benjamin Hanzelmann
 *
 */
@RunWith( ConfigurationRunner.class )
@Configurators( DefaultSuiteConfigurator.class )
public class ConsumerTest
    extends BehaviourSuiteConfiguration<Consumer>
{
    private Consumer consumer = new Consumer();

    @Test
    public void testConsumer()
        throws Exception
    {
        URL url = new URL( url( "foo" ) );

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput( true );

        byte[] pattern = new byte[64];
        for ( int i = 0; i < pattern.length; i++ )
        {
            pattern[i] = (byte) ( 33 + i );
        }
        int count = 1024 * 1024 * 16;
        count = 16;
        int targetSize = count * pattern.length;
        
        conn.setFixedLengthStreamingMode( targetSize );
        conn.connect();
        OutputStream out = conn.getOutputStream();

        int total = 0;
        for ( int i = 0; i < count; i++ )
        {
            out.write( pattern );
            total += pattern.length;
        }
        
        out.flush();

        out.close();

        assertEquals( 200, conn.getResponseCode() );
        conn.disconnect();

        assertEquals( total, targetSize );
        assertEquals( total, behaviour().getTotal() );
    }


    @Override
    protected Consumer behaviour()
    {
        return consumer;
    }
}
