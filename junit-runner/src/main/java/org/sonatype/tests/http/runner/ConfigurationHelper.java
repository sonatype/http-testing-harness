/*
 * Copyright (c) 2010-2014 Sonatype, Inc. All rights reserved.
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
package org.sonatype.tests.http.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.tests.http.runner.annotations.ConfiguratorList;
import org.sonatype.tests.http.runner.annotations.Configurators;
import org.sonatype.tests.http.runner.annotations.IgnoreConfigurators;

/**
 * A Helper class for the tasks needed by {@link Junit3SuiteConfiguration} and {@link ConfigurationRunner} as well.
 * 
 * @author Benjamin Hanzelmann
 */
public class ConfigurationHelper
{

    /**
     * Use the annotations for the given class to compute the configurators to use.
     * 
     * @param testClass the class to scan.
     * @return the configured configurators.
     * @see Configurators
     * @see ConfiguratorList
     * @see IgnoreConfigurators
     */
    public static List<SuiteConfigurator> computeConfigurators( Class<?> testClass )
    {
        List<SuiteConfigurator> configurators = new LinkedList<SuiteConfigurator>();
        List<Class<? extends SuiteConfigurator>> defaultConfiguratorClasses = getDefaultConfiguratorClasses();
        try
        {
            Configurators anno = testClass.getAnnotation( Configurators.class );
            ConfiguratorList list = testClass.getAnnotation( ConfiguratorList.class );
            List<Class<? extends SuiteConfigurator>> configuratorClasses =
                new LinkedList<Class<? extends SuiteConfigurator>>();
            if ( anno != null )
            {
                configuratorClasses = Arrays.asList( anno.value() );
            }
            else if ( list != null )
            {
                configuratorClasses = getConfiguratorClasses( list.getClass().getClassLoader(), list.value() );
            }
            else if ( defaultConfiguratorClasses != null )
            {
                configuratorClasses = defaultConfiguratorClasses;
            }

            configuratorClasses.removeAll( computeIgnoredConfiguratorClasses( testClass ) );

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
        return configurators;
    }

    /**
     * Use the annotations for the given class to compute the configurators to ignore.
     * 
     * @param testClass the class to scan.
     * @return the ignored configurators.
     * @see ConfiguratorList
     * @see IgnoreConfigurators
     */
    public static Collection<?> computeIgnoredConfiguratorClasses( Class<?> testCls )
    {

        LinkedList<Class<? extends SuiteConfigurator>> ret = new LinkedList<Class<? extends SuiteConfigurator>>();
        IgnoreConfigurators anno = testCls.getAnnotation( IgnoreConfigurators.class );
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
     * Load the default configurator list ("DefaultSuiteConfigurator.list").
     * 
     * @return the configurator classes defined in the list.
     */
    public static List<Class<? extends SuiteConfigurator>> getDefaultConfiguratorClasses()
    {
        return getConfiguratorClasses( null, "DefaultSuiteConfigurator.list" );
    }

    /**
     * Load the configurators mentioned in the given lists, using the given classloader.
     * 
     * @param cl the classloader to use.
     * @param lists the lists to read.
     * @return the configurator classes.
     */
    public static List<Class<? extends SuiteConfigurator>> getConfiguratorClasses( ClassLoader cl, String... lists )
    {
        ClassLoader realCl = cl;
        if ( realCl == null )
        {
            realCl = ClassLoader.getSystemClassLoader();
        }

        List<Class<? extends SuiteConfigurator>> classes = new LinkedList<Class<? extends SuiteConfigurator>>();

        BufferedReader in = null;
        try
        {
            for ( String list : lists )
            {
                Enumeration<URL> resources = realCl.getResources( list );
                if ( resources.hasMoreElements() == false )
                {
                    // fall back to file
                    final File file = new File( list );
                    if ( file.exists() )
                    {
                        resources = new Enumeration<URL>()
                        {

                            private boolean taken = false;

                            public boolean hasMoreElements()
                            {
                                return !taken;
                            }

                            public URL nextElement()
                            {
                                try
                                {
                                    taken = true;
                                    return file.toURI().toURL();
                                }
                                catch ( MalformedURLException e )
                                {
                                    String msg = "No resource found, tried to load list as file but: ";
                                    throw new IllegalArgumentException( msg + e.getMessage(), e );
                                }
                            }

                        };
                    }
                    else if ( cl != null )
                    {
                        throw new IllegalArgumentException( "Neither resource nor file found for configurator list: "
                            + list );
                    }

                }
                while ( resources.hasMoreElements() )
                {
                    URL url = resources.nextElement();
                    in = new BufferedReader( new InputStreamReader( url.openStream() ) );
                    String clsName;
                    while ( ( clsName = in.readLine() ) != null )
                    {
                        @SuppressWarnings( "unchecked" )
                        Class<? extends SuiteConfigurator> cls =
                            (Class<? extends SuiteConfigurator>) realCl.loadClass( clsName );

                        classes.add( cls );
                    }
                }
            }
            if ( classes.isEmpty() && cl != null )
            {
                throw new IllegalArgumentException( "Cannot find specified configurator list: "
                    + Arrays.toString( lists ) );
            }
            return classes;
        }
        catch ( Exception t )
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

}
