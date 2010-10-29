package org.sonatype.tests.jetty.runner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import org.sonatype.tests.runner.api.SuiteConfigurator;

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