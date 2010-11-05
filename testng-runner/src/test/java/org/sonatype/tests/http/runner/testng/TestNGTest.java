package org.sonatype.tests.http.runner.testng;

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


import org.sonatype.tests.http.runner.annotations.Configurators;
import org.sonatype.tests.http.runner.testng.TestNGSuiteConfiguration;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Benjamin Hanzelmann
 */
@Test
@Configurators( { TestConfigurator.class, CopyOfTestConfigurator.class } )
public class TestNGTest
    extends TestNGSuiteConfiguration
{

    @Factory
    public Object[] configurationTests()
        throws Exception
    {
        return TestNGSuiteConfiguration.testNGFactory( getClass() );
    }

    public void testRunner1()
    {
        System.err.println( "test" );
    }

    public void testRunner2()
    {
        System.err.println( "test2" );
    }


    public String getTestName()
    {
        return configurator().getName();
    }


}
