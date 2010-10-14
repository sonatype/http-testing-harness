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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.sonatype.tests.runner.api.SuiteConfiguration;
import org.sonatype.tests.runner.api.SuiteConfigurator;

/**
 * Junit4 test runner supporting multiple server configurations for the test class. This runner can only execute tests
 * from classes that extend/implement {@link DefaultSuiteConfiguration} or {@link DefaultSuiteConfiguration}.
 * <p>
 * The runner provides annotations to configure the used configurations.
 * {@link org.sonatype.tests.jetty.runner.ConfigurationRunner.Configurators} takes {@link SuiteConfigurator} classes and
 * uses them to run every test method. Every entry in
 * {@link org.sonatype.tests.jetty.runner.ConfigurationRunner.ConfiguratorList} will be loaded as a resource. The runner
 * expects every line of the resource to be the class name of a SuiteConfigurator.
 * {@link org.sonatype.tests.jetty.runner.ConfigurationRunner.IgnoreConfigurators} may be used to to ignore
 * configurators from the list.
 * <p>
 * If none of the annotations are present, the runner tries to load a default list (
 * <code>DefaultSuiteConfigurators.list</code>).
 * 
 * @author Benjamin Hanzelmann
 * @see SuiteConfigurator
 * @see DefaultSuiteConfiguration
 * @see DefaultSuiteConfiguration
 */
public class ConfigurationRunner
    extends BlockJUnit4ClassRunner
{

    private List<Class<? extends SuiteConfigurator>> defaultConfiguratorClasses;

    public ConfigurationRunner( Class<?> klass )
        throws InitializationError
    {
        super( klass );
        if ( !SuiteConfiguration.class.isAssignableFrom( klass ) )
        {
            IllegalArgumentException error =
                new IllegalArgumentException( "Can only run tests inheriting from SuiteConfiguration." );
            throw new InitializationError( error );
        }
    }

    /**
     * The annotation to set used configurators directly.
     * 
     * @author Benjamin Hanzelmann
     * @see SuiteConfigurator
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.TYPE )
    @Inherited
    public @interface Configurators
    {
        public Class<? extends SuiteConfigurator>[] value();
    }

    /**
     * This annotation may be used to filter a configurator list.
     * 
     * @author Benjamin Hanzelmann
     * @see ConfiguratorList
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.TYPE )
    @Inherited
    public @interface IgnoreConfigurators
    {
        public Class<? extends SuiteConfigurator>[] value();
    }

    /**
     * This annotation may be used to set a list of configurators. It loads the given resources and expects the file to
     * contain the class names of configurators, one per line.
     * 
     * @author Benjamin Hanzelmann
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.TYPE )
    @Inherited
    public @interface ConfiguratorList
    {
        public String[] value();
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods()
    {
        if ( defaultConfiguratorClasses == null )
        {
            initDefaultConfiguratorClasses();
        }
        List<SuiteConfigurator> configurators = new LinkedList<SuiteConfigurator>();
        try
        {
            Configurators anno = getTestClass().getJavaClass().getAnnotation( Configurators.class );
            ConfiguratorList list = getTestClass().getJavaClass().getAnnotation( ConfiguratorList.class );
            List<Class<? extends SuiteConfigurator>> configuratorClasses =
                new LinkedList<Class<? extends SuiteConfigurator>>();
            if ( anno != null )
            {
                configuratorClasses = Arrays.asList( anno.value() );
            }
            else if ( list != null )
            {
                configuratorClasses = getConfiguratorClasses( list.value() );
            }
            else if ( defaultConfiguratorClasses != null )
            {
                configuratorClasses = defaultConfiguratorClasses;
                configuratorClasses.removeAll( computeIgnoredConfiguratorClasses() );
            }

            for ( Class<? extends SuiteConfigurator> cfgClass : configuratorClasses )
            {
                Constructor<? extends SuiteConfigurator> con = cfgClass.getConstructor();
                SuiteConfigurator configurator = con.newInstance();
                configurators.add( configurator );
            }
        }
        catch ( Throwable e )
        {
            throw new IllegalStateException( "Configuration error: " + e.getMessage(), e );
        }

        List<FrameworkMethod> methods = super.computeTestMethods();

        List<FrameworkMethod> cfgMethods = new LinkedList<FrameworkMethod>();
        for ( FrameworkMethod method : methods )
        {
            for ( SuiteConfigurator cfg : configurators )
            {
                cfgMethods.add( new ConfiguratorMethod( method.getMethod(), cfg ) );
            }
        }
        return cfgMethods;
    }

    private Collection<?> computeIgnoredConfiguratorClasses()
    {
        LinkedList<Class<? extends SuiteConfigurator>> ret = new LinkedList<Class<? extends SuiteConfigurator>>();
        IgnoreConfigurators anno = getTestClass().getJavaClass().getAnnotation( IgnoreConfigurators.class );
        if ( anno != null )
        {
            for ( Class<? extends SuiteConfigurator> cls : anno.value() )
            {
                ret.add( cls );
            }
        }
        return ret;
    }

    /**
     * Load list of configurators from all resources named "SuiteConfigurator.list". (One full class name per line.)
     */
    private void initDefaultConfiguratorClasses()
    {
        defaultConfiguratorClasses = getConfiguratorClasses( "DefaultSuiteConfigurator.list" );
    }

    private List<Class<? extends SuiteConfigurator>> getConfiguratorClasses( String... lists )
    {
        List<Class<? extends SuiteConfigurator>> classes = new LinkedList<Class<? extends SuiteConfigurator>>();

        BufferedReader in = null;
        try
        {
            for ( String list : lists )
            {
                Enumeration<URL> resources = getClass().getClassLoader().getResources( list );
                while ( resources.hasMoreElements() )
                {
                    URL url = resources.nextElement();
                    in = new BufferedReader( new InputStreamReader( url.openStream() ) );
                    String clsName;
                    while ( ( clsName = in.readLine() ) != null )
                    {
                        System.err.println( "using " + clsName + " as configurator." );

                        @SuppressWarnings( "unchecked" )
                        Class<? extends SuiteConfigurator> cls =
                            (Class<? extends SuiteConfigurator>) getClass().getClassLoader().loadClass( clsName );

                        classes.add( cls );
                    }
                }
            }
            if ( classes.isEmpty() )
            {
                throw new IllegalStateException( "Cannot find default configurator list" );
            }
            return classes;
        }
        catch ( Throwable t )
        {
            throw new RuntimeException( t );
        }
        finally
        {
            if ( in != null )
            {
                try
                {
                    in.close();
                }
                catch ( IOException e )
                {
                }
            }
        }
    }

    @Override
    protected Statement methodInvoker( FrameworkMethod method, Object test )
    {
        SuiteConfiguration cfg = SuiteConfiguration.class.cast( test );
        ConfiguratorMethod cfgMethod = ConfiguratorMethod.class.cast( method );
        cfg.setConfigurator( cfgMethod.getConfigurator() );

        return super.methodInvoker( method, test );
    }

}