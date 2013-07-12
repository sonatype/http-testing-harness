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
package org.sonatype.tests.http.runner.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.sonatype.tests.http.runner.annotations.Configurators;
import org.sonatype.tests.http.runner.api.SuiteConfigurator;
import org.sonatype.tests.http.runner.junit.Junit3SuiteConfiguration;
import org.sonatype.tests.http.runner.junit.Junit3SupportTest.TestConfigurator;
import org.sonatype.tests.http.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
@Configurators( TestConfigurator.class )
public class Junit3SupportTest
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
        TestSuite suite = Junit3SuiteConfiguration.suite( Junit3SupportTest.class );
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
