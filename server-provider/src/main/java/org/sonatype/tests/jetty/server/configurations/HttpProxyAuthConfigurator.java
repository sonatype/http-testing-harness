package org.sonatype.tests.jetty.server.configurations;

import org.sonatype.tests.jetty.server.impl.JettyProxyProvider;
import org.sonatype.tests.server.api.ServerProvider;

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