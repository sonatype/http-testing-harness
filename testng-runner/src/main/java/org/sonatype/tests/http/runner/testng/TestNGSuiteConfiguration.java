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

import java.util.LinkedList;
import java.util.List;

import org.sonatype.tests.http.runner.AbstractSuiteConfiguration;
import org.sonatype.tests.http.runner.ConfigurationHelper;
import org.sonatype.tests.http.runner.api.SuiteConfigurator;
import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * @author Benjamin Hanzelmann
 */
public class TestNGSuiteConfiguration
    extends AbstractSuiteConfiguration
    implements ITest
{

    @Override
    @BeforeMethod
    public void before()
        throws Exception
    {
        super.before();
    }

    @Override
    @AfterMethod
    public void after()
        throws Exception
    {
        super.after();
    }

    public static Object[] testNGFactory( Class<? extends TestNGSuiteConfiguration> cls )
        throws Exception
    {
        List<SuiteConfigurator> configurators = ConfigurationHelper.computeConfigurators( cls );
        List<TestNGSuiteConfiguration> tests = new LinkedList<TestNGSuiteConfiguration>();
        for ( SuiteConfigurator cfg : configurators )
        {
            TestNGSuiteConfiguration test = cls.getConstructor().newInstance();
            test.setConfigurator( cfg );
            tests.add( test );
        }
    
        return tests.toArray();
    }

    /*
     * (non-Javadoc)
     * @see org.testng.ITest#getTestName()
     */
    public String getTestName()
    {
        return configurator().getName();
    }

}
