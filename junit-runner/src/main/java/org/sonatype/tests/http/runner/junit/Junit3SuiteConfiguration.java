package org.sonatype.tests.http.runner.junit;

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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.sonatype.tests.http.runner.ConfigurationHelper;
import org.sonatype.tests.http.runner.api.SuiteConfigurator;
import org.sonatype.tests.http.server.api.ServerProvider;

/**
 * Junit3 test case supporting multiple server configurations for the test class.
 * <p>
 * Annotations are provided to configure the used configurations.
 * {@link org.sonatype.tests.http.runner.annotations.Configurators} takes {@link SuiteConfigurator} classes and
 * uses them to run every test method. Every entry in
 * {@link org.sonatype.tests.http.runner.annotations.ConfiguratorList} will be loaded as a resource. The runner
 * expects every line of the resource to be the class name of a SuiteConfigurator.
 * {@link org.sonatype.tests.http.runner.annotations.IgnoreConfigurators} may be used to to ignore
 * configurators from the list.
 * <p>
 * If none of the annotations are present, the runner tries to load a default list (
 * <code>DefaultSuiteConfigurators.list</code>).
 * 
 * @author Benjamin Hanzelmann
 * @see SuiteConfigurator
 * @see DefaultSuiteConfiguration
 */
public abstract class Junit3SuiteConfiguration
    extends TestCase
{
    private ServerProvider provider;

    private SuiteConfigurator configurator;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        if ( provider != null )
        {
            provider.stop();
            provider = null;
        }

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
        provider.stop();
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
        try
        {
            return provider.getUrl().toExternalForm();
        }
        catch ( MalformedURLException e )
        {
            throw new IllegalArgumentException( "Provider was set up with wrong url" );
        }
    }

    public ServerProvider provider()
    {
        return provider;
    }

    public String url( String path, String... parts )
    {
        try
        {
            String url = url() + "/" + path;
            for ( String part : parts )
            {
                part = URLEncoder.encode( part, "UTF-8" );
                url += "/" + part;
            }

            // logger.debug( "returning url... " + url );

            return url;
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    /**
     * Create a {@link TestSuite} containing all tests for the given class. A test consists of a test method declared in
     * or inherited by the class, combined with a {@link SuiteConfigurator}.
     * 
     * @param cls the class to scan.
     * @return the test suite to run.
     * @throws Exception when reflection fails with the given class.
     */
    public static TestSuite suite( Class<? extends Junit3SuiteConfiguration> cls )
        throws Exception
    {
        TestSuite suite = new TestSuite();
        suite.setName( cls.getName() );
    
        List<Method> testMethods = new LinkedList<Method>();
    
        Method[] methods = cls.getMethods();
        for ( Method method : methods )
        {
            if ( method.getName().startsWith( "test" ) )
            {
                testMethods.add( method );
            }
        }
    
    
        Junit3SuiteConfiguration test = cls.getConstructor().newInstance();
    
        List<SuiteConfigurator> cfgs = ConfigurationHelper.computeConfigurators( cls );
    
        for ( SuiteConfigurator cfg : cfgs )
        {
            for ( Method method : testMethods )
            {
                ConfiguratorTest cfgTest = new ConfiguratorTest( method, test, cfg );
                cfgTest.setName( String.format( "%s %s", method.getName(), cfg.getName() ) );
                suite.addTest( cfgTest );
            }
        }
        
        return suite;
    }

}
