/*
 * Copyright (c) 2010-2013 Sonatype, Inc. All rights reserved.
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
