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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.sonatype.tests.runner.api.SuiteConfiguration;
import org.sonatype.tests.runner.api.SuiteConfigurator;
import org.sonatype.tests.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
public abstract class DefaultSuiteConfiguration
    implements SuiteConfiguration
{

	// protected Logger logger = LoggerFactory.getLogger( this.getClass() );

    private boolean doClassInit = true;

    private SuiteConfigurator configurator;

    private static ServerProvider provider;

    public void setConfigurator( SuiteConfigurator configurator )
    {
        this.configurator = configurator;
    }

    @Before
    public void before()
        throws Exception
    {
        if ( doClassInit )
        {
            provider = configurator.provider();
            doClassInit = false;
            configureProvider( provider );
            provider.start();
        }
    }


    protected void configureProvider( ServerProvider provider )
    {
    }

    @After
    public void after()
        throws Exception
    {
        // provider.stop();
    }

    @AfterClass
    public static void afterClass()
        throws Exception
    {
        if ( provider != null )
        {
            provider.stop();
        }
    }

    protected String url()
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

    protected ServerProvider provider()
    {
        return provider;
    }

    protected String url( String path, String... parts )
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

}
