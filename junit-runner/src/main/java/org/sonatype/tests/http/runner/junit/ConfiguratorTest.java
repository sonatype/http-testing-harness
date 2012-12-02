/*
 * Copyright (c) 2010-2012 Sonatype, Inc. All rights reserved.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import org.sonatype.tests.http.runner.api.SuiteConfigurator;

/**
 * @author Benjamin Hanzelmann
 */
final class ConfiguratorTest
    extends TestCase
    implements Test
{
    private SuiteConfigurator cfg;

    private Junit3SuiteConfiguration test;

    private Method method;

    @Override
    public void run( TestResult result )
    {
        test.setConfigurator( cfg );
        result.startTest( this );
        Protectable p = new Protectable()
        {
            public void protect()
                throws Throwable
            {
                test.setUp();
                try
                {
                    method.invoke( test );
                }
                catch ( InvocationTargetException e )
                {
                    throw e.getCause();
                }
                test.tearDown();
            }
        };
        result.runProtected( this, p );
        result.endTest( this );
    }

    public ConfiguratorTest( Method method, Junit3SuiteConfiguration test, SuiteConfigurator cfg )
    {
        this.method = method;
        this.test = test;
        this.cfg = cfg;
    }

    @Override
    public int countTestCases()
    {
        return 1;
    }

    @Override
    public String toString()
    {
        return getName() + "(" + test.getClass().getName() + ")";
    }

}