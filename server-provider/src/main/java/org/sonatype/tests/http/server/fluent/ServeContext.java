package org.sonatype.tests.http.server.fluent;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.Lists;
import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.api.ServerProvider;

/**
 * @since 1.0
 */
public class ServeContext
{

    private final ServerProvider serverProvider;

    private final String pattern;

    private final Server server;

    private List<Behaviour> behaviours = Lists.newArrayList();

    ServeContext( final ServerProvider serverProvider, final String pattern, final Server server )
    {
        this.serverProvider = serverProvider;
        this.pattern = pattern;
        this.server = server;
    }

    public Server withBehaviours( Behaviour... behaviours )
    {
        checkNotNull(behaviours);

        serverProvider.addBehaviour( pattern, behaviours );

        return server;
    }

}
