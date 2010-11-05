package org.sonatype.tests.auth;

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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.tests.async.util.AsyncSuiteConfiguration;
import org.sonatype.tests.http.runner.junit.ConfigurationRunner;
import org.sonatype.tests.http.server.jetty.behaviour.Content;
import org.sonatype.tests.http.server.jetty.behaviour.NTLMAuth;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.Response;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

/**
 * @author Benjamin Hanzelmann
 *
 */
@RunWith( ConfigurationRunner.class )
public class NTLMTest
    extends AsyncSuiteConfiguration
{

    @Test
    public void testDummy()
    {

    }

    @Test
    @Ignore( "Does not work on windows (erroneous type2 message?), NTLMAuth is not complete anyway." )
    public void testNTLM()
        throws Exception
    {
        provider().stop();
        provider().addBehaviour( "/content/*", new NTLMAuth(), new Content() );
        provider().start();

        AsyncHttpClient c = new AsyncHttpClient( new NettyAsyncHttpProvider( builder().build() ), builder().build() );

        BoundRequestBuilder rb = c.prepareGet( url( "content", "test" ) );
        Realm realm =
            new Realm.RealmBuilder().setDomain( "Test Server" ).setScheme( AuthScheme.NTLM ).setUsePreemptiveAuth( false ).build();
        rb.setRealm( realm );
        Response response = execute( rb );

        assertEquals( "test", response.getResponseBody() );
    }

}
