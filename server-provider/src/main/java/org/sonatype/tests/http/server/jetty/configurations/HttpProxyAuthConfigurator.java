package org.sonatype.tests.http.server.jetty.configurations;

import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.impl.JettyProxyProvider;

/**
 * @author Benjamin Hanzelmann
 */
public class HttpProxyAuthConfigurator
    extends DefaultSuiteConfigurator
{

    @Override
    public String getName()
    {
        return super.getName() + " AUTHPROXY ";
    }

    private JettyProxyProvider provider;

    public HttpProxyAuthConfigurator()
    {
        try
        {
            provider = new JettyProxyProvider( "puser", "password" );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( e );
        }
    }

    @Override
    public ServerProvider provider()
    {
        return provider;
    }

}