package org.sonatype.tests.async.util;

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

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.tests.http.runner.junit.DefaultSuiteConfiguration;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.RealmBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.logging.LogManager;
import com.ning.http.client.logging.LoggerProvider;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class AsyncSuiteConfiguration
    extends DefaultSuiteConfiguration
{

    protected Realm realm;

    private int timeout = 2000;

    protected AsyncHttpClient client()
    {
        return new AsyncHttpClient( settings( builder() ).build() );
    }

    /**
     * @return
     */
    protected Builder builder()
    {
        return new Builder();
    }

    protected Response executeGet( String url )
        throws Exception
    {
        BoundRequestBuilder rb = client().prepareGet( url );
        requestSettings( rb );
        Response response = rb.execute().get();
        return response;
    }

    protected Builder settings( Builder rb )
    {
        rb.setFollowRedirects( true );
        rb.setConnectionTimeoutInMs( timeout );
        rb.setIdleConnectionTimeoutInMs( timeout );
        rb.setRequestTimeoutInMs( timeout );
        rb.setMaximumNumberOfRedirects( 5 );
        return rb;
    }

    protected BoundRequestBuilder requestSettings( BoundRequestBuilder rb )
    {
        if ( realm != null )
        {
            rb.setRealm( realm );
        }
        return rb;
    }

    protected void setAuthentication( String principal, String password, boolean usePreemptiveAuth )
    {
        RealmBuilder realmbuilder = new Realm.RealmBuilder();
        realmbuilder.setPrincipal( principal );
        realmbuilder.setPassword( password );
        realmbuilder.setUsePreemptiveAuth( usePreemptiveAuth );
        realm = realmbuilder.build();
    }

    protected Response execute( BoundRequestBuilder rb )
        throws Exception
    {
        return requestSettings( rb ).execute().get();
    }

    @Override
    public void configureProvider( ServerProvider provider )
    {
        super.configureProvider( provider );
        if ( JettyServerProvider.class.isAssignableFrom( provider.getClass() ) )
        {
            ((JettyServerProvider)provider).addDefaultServices();
        }
    }

    public static void setUpLogger() {
        LogManager.setProvider(new LoggerProvider() {
    
            public com.ning.http.client.logging.Logger getLogger(final Class<?> clazz) {
                return new com.ning.http.client.logging.Logger() {

                    Logger log = LoggerFactory.getLogger( "com.ning.http.client" );
    
                    public boolean isDebugEnabled() {
                        return log.isDebugEnabled();
                    }
    
                    public void debug(final String msg, final Object... msgArgs) {
                        log.debug( msg, msgArgs );
                    }
    
                    public void debug(final Throwable t) {
                        log.debug( t.getMessage(), t );
                    }
    
                    public void debug(final Throwable t, final String msg, final Object... msgArgs) {
                        log.debug( msg, msgArgs );
                        log.debug( t.getMessage(), t );
                    }
    
                    public void info(final String msg, final Object... msgArgs) {
                        log.info( msg, msgArgs );
                    }
    
                    public void info(final Throwable t) {
                        log.info( t.getMessage(), t );
                    }
    
                    public void info(final Throwable t, final String msg, final Object... msgArgs) {
                        log.info( msg, msgArgs );
                        log.info( t.getMessage(), t );
                    }
    
                    public void warn(final String msg, final Object... msgArgs) {
                        log.warn( msg, msgArgs );
                    }
    
                    public void warn(final Throwable t) {
                        log.warn( t.getMessage(), t );
                    }
    
                    public void warn(final Throwable t, final String msg, final Object... msgArgs) {
                        log.warn( msg, msgArgs );
                        log.warn( t.getMessage(), t );
                    }
    
                    public void error(final String msg, final Object... msgArgs) {
                        log.error( msg, msgArgs );
                    }
    
                    public void error(final Throwable t) {
                        log.error( t.getMessage(), t );
                    }
    
                    public void error(final Throwable t, final String msg, final Object... msgArgs) {
                        log.error( msg, msgArgs );
                        log.error( t.getMessage(), t );
                    }
                };
            }
        });
    }

    @BeforeClass
    public static void beforeClass()
        throws Exception
    {
        setUpLogger();
    }

    public void setTimeout( int timeout )
    {
        this.timeout = timeout;
    }

}
