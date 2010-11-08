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


    @Override
    public ServerProvider provider()
    {
        try
        {
            return new JettyProxyProvider( "puser", "password" );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( e.getMessage(), e );
        }
    }

}