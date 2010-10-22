package org.sonatype.tests.jetty.runner;

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

import junit.framework.TestCase;

import org.sonatype.tests.runner.api.SuiteConfigurator;
import org.sonatype.tests.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
public abstract class Junit3SuiteConfiguration
    extends TestCase
{
    
    private final DefaultSuiteConfiguration cfg = new DefaultSuiteConfiguration()
    {
    };

    private ServerProvider provider;

    private SuiteConfigurator configurator;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        provider = configurator.provider();
        if ( provider == null )
        {
            throw new IllegalArgumentException( "Configurator failed, provider is null." );
        }
        configureProvider( provider );
        provider.start();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void setConfigurator( SuiteConfigurator configurator )
    {
        this.configurator = configurator;
    }

    public void configureProvider( ServerProvider provider )
    {
    }

    public String url()
    {
        return cfg.url();
    }

    public ServerProvider provider()
    {
        return provider;
    }

    public String url( String path, String... parts )
    {
        return cfg.url( path, parts );
    }

}
