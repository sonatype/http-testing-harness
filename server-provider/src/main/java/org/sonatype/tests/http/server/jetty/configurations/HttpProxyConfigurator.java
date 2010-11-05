package org.sonatype.tests.http.server.jetty.configurations;

import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.impl.JettyProxyProvider;

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