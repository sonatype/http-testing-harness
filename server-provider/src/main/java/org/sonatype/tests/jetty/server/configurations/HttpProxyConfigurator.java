package org.sonatype.tests.jetty.server.configurations;

import org.sonatype.tests.jetty.server.impl.JettyProxyProvider;
import org.sonatype.tests.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
public class HttpProxyConfigurator
    extends DefaultSuiteConfigurator
{
    @Override
    public String getName()
    {
        return super.getName() + " PROXY ";
    }

    private JettyProxyProvider provider;

    public HttpProxyConfigurator()
    {
        try
        {
            provider = new JettyProxyProvider();
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