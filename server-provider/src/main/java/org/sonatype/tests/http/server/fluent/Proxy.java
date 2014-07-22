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
package org.sonatype.tests.http.server.fluent;

import org.sonatype.tests.http.server.jetty.impl.JettyProxyProvider;

/**
 * This is not a real proxy. It supports proxy connections and auth, but will not forward requests to the requested URL.
 * It will resolve the requested path against the behaviors set for the proxy instance.
 * <p/>
 * Example:
 * <pre>
 *     Server proxy = Proxy.withPort(0).start();
 *     proxy.serve( "/*" ).withBehaviours( redirect( "/other/url" ) );
 *     proxy.serve( "/other/*" ).withBehaviours( content( "test" ) );
 *     setProxy("localhost", proxy.getPort() );
 *     assertThat( get( "http://invalid.url/foo" ), is ( "test" ) );
 * </pre>
 */
public class Proxy
    extends Server
{

    public Proxy( final JettyProxyProvider serverProvider )
    {
        super( serverProvider );
    }

    public static Server withPort( int port )
    {
        JettyProxyProvider provider = new JettyProxyProvider();
        provider.setPort( port );
        return new Server( provider );
    }

}
