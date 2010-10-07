package org.sonatype.tests.general;

import org.sonatype.tests.jetty.server.api.ServerProvider;
import org.sonatype.tests.jetty.server.api.SuiteConfigurator;
import org.sonatype.tests.jetty.server.impl.JettyServerProvider;
import org.sonatype.tests.jetty.server.suites.SimpleTestSuite;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;

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
public class SimpleHttpTest
    extends SimpleTestSuite
{

    public static class HttpSuiteConfigurator
        implements SuiteConfigurator
    {

        public AsyncHttpClient newConnector()
        {
            Builder cfg = builder();

            AsyncHttpClient client = new AsyncHttpClient( cfg.build() );
            return client;
        }

        private Builder builder()
        {
            Builder cfg = new AsyncHttpClientConfig.Builder();
            cfg.setConnectionTimeoutInMs( 2000 );
            cfg.setIdleConnectionTimeoutInMs( 2000 );
            cfg.setRequestTimeoutInMs( 2000 );
            cfg.setFollowRedirects( true );
            return cfg;
        }

        public ServerProvider provider()
        {
            ServerProvider provider;
            try
            {
                provider = new JettyServerProvider();
            }
            catch ( Exception e )
            {
                throw new IllegalStateException( e.getMessage(), e );
            }
            return provider;
        }
    }

    public SimpleHttpTest()
    {
        super( new HttpSuiteConfigurator() );
    }

}
