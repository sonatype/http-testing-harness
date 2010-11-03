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


import javax.net.ssl.SSLContext;

import org.sonatype.tests.async.util.CertUtil;
import org.sonatype.tests.jetty.runner.ConfigurationRunner.Configurators;
import org.sonatype.tests.jetty.server.configurations.CertAuthSuiteConfigurator;
import org.sonatype.tests.server.api.ServerProvider;

import com.ning.http.client.AsyncHttpClientConfig.Builder;

/**
 * @author Benjamin Hanzelmann
 */
@Configurators( CertAuthSuiteConfigurator.class )
public class CertAuthGetTest
    extends SimpleGetTest
{

    private String keystorePath = "src/test/resources/client.keystore";

    private String keystorePass = "password";

    private String alias = "client";

    @Override
    protected Builder builder()
    {
        return super.builder().setSSLContext( sslContext() );

    }

    private SSLContext sslContext()
    {
        return CertUtil.sslContext( keystorePath, keystorePass, alias );
    }

    @Override
    public void configureProvider( ServerProvider provider )
    {
        super.configureProvider( provider );
        provider.addUser( alias, CertUtil.getCertificate( alias, keystorePath, keystorePass ) );
    }

}
