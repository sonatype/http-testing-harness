package org.sonatype.tests.custom;

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

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.tests.async.util.AsyncSuiteConfiguration;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;
import org.sonatype.tests.jetty.runner.ConfigurationRunner.ConfiguratorList;
import org.sonatype.tests.jetty.runner.ConfigurationRunner.IgnoreConfigurators;
import org.sonatype.tests.jetty.server.configurations.DigestAuthSslSuiteConfigurator;
import org.sonatype.tests.jetty.server.configurations.DigestAuthSuiteConfigurator;

import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Response;

/**
 * @author Benjamin Hanzelmann
 *
 */
@RunWith( ConfigurationRunner.class )
@ConfiguratorList( "AuthSuiteConfigurator.list" )
// AHC Bug: Will always use Basic-Auth
@IgnoreConfigurators( { DigestAuthSuiteConfigurator.class, DigestAuthSslSuiteConfigurator.class } )
public class FailingAuthTest
    extends AsyncSuiteConfiguration
{

    private boolean preemptive = true;

    @Test
    public void testSuccessful()
        throws Exception
    {
        setAuthentication( "user", "password", preemptive );
        String content = "someContent";
        String url = url( "content", content );
        Response response = executeGet( url );
        String body = response.getResponseBody();

        assertEquals( content, body );
    }

    @Test
    public void testNoRealm()
        throws Exception
    {
        String content = "someContent";
        String url = url( "content", content );
        Response response = executeGet( url );

        assertEquals( 401, response.getStatusCode() );
    }

    @Override
    protected Builder settings( Builder rb )
    {
        return super.settings( rb ).setRequestTimeoutInMs( 60000 ).setConnectionTimeoutInMs( 60000 ).setIdleConnectionTimeoutInMs( 60000 );
    }

}
