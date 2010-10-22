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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.sonatype.tests.jetty.runner.ConfigurationRunner.Configurators;
import org.sonatype.tests.jetty.runner.Junit3Support.TestConfigurator;
import org.sonatype.tests.runner.api.SuiteConfigurator;
import org.sonatype.tests.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
@Configurators( TestConfigurator.class )
public class Junit3Support
    extends Junit3SuiteConfiguration
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

    public static Test suite()
        throws Exception
    {
        TestSuite suite = ConfigurationHelper.suite( Junit3Support.class );
        return suite;
    }

    public void testSucceed()
    {

    }

    @Override
    public void configureProvider( ServerProvider provider )
    {
        assertEquals( p, provider );
        super.configureProvider( provider );
    }

}
