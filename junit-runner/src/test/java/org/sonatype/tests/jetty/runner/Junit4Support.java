package org.sonatype.tests.jetty.runner;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.sonatype.tests.jetty.runner.ConfigurationRunner.Configurators;
import org.sonatype.tests.jetty.runner.Junit4Support.TestConfigurator;
import org.sonatype.tests.runner.api.SuiteConfigurator;
import org.sonatype.tests.server.api.ServerProvider;

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


/**
 * @author Benjamin Hanzelmann
 *
 */
@RunWith( ConfigurationRunner.class )
@Configurators( TestConfigurator.class )
public class Junit4Support
    extends DefaultSuiteConfiguration
{
    private static DummyProvider p = new DummyProvider();

    public static class TestConfigurator
        implements SuiteConfigurator
    {

        public ServerProvider provider()
        {
            return p;
        }

        public String getName()
        {
            return "Test";
        }

    }


    @org.junit.Test
    public void succeed()
    {

    }
    
    @Override
    public void configureProvider( ServerProvider provider )
    {
        Assert.assertEquals( p, provider );
        super.configureProvider( provider );
    }
}
