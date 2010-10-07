package org.sonatype.tests.general.proxy;

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

import org.sonatype.tests.jetty.server.api.ServerProvider;
import org.sonatype.tests.jetty.server.impl.JettyProxyProvider;
import org.sonatype.tests.jetty.server.impl.JettyServerProvider;
import org.sonatype.tests.jetty.server.suites.SimpleTestSuite;
import org.sonatype.tests.jetty.server.util.DefaultSuiteConfigurator;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ProxyServer;

/**
 * @author Benjamin Hanzelmann
 */
public class SimpleHttpProxyTest
    extends SimpleTestSuite
{

    /**
     * @author Benjamin Hanzelmann
     */
    public static class HttpProxyConfigurator
        extends DefaultSuiteConfigurator
    {
        private JettyProxyProvider provider;
    
        public HttpProxyConfigurator()
        {
            try
            {
                ServerProvider realServer = new JettyServerProvider();
                realServer.setPort( 8887 );
                provider = new JettyProxyProvider( realServer );
            }
            catch ( Exception e )
            {
                throw new IllegalStateException( e );
            }
        }
    
        @Override
        public AsyncHttpClient newConnector()
        {
            super.builder().setProxyServer( new ProxyServer( "localhost", provider.getPort() )
            {

                @Override
                public int getPort()
                {
                    return provider.getPort();
                }

            } );
            return super.newConnector();
        }
    
        @Override
        public ServerProvider provider()
        {
            return provider;
        }
    
    }

    /**
     * @param configurator
     */
    public SimpleHttpProxyTest()
    {
        super( new HttpProxyConfigurator() );
    }

}
