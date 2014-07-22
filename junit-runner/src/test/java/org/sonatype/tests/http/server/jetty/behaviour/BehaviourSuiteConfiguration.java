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
package org.sonatype.tests.http.server.jetty.behaviour;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Before;
import org.sonatype.tests.http.runner.AbstractSuiteConfiguration;
import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 *
 */
public abstract class BehaviourSuiteConfiguration<T extends Behaviour>
    extends AbstractSuiteConfiguration
{
    public static final class CustomTrustManager
        implements X509TrustManager
    {

        public void checkClientTrusted( X509Certificate[] arg0, String arg1 )
            throws CertificateException
        {
        }

        public void checkServerTrusted( X509Certificate[] arg0, String arg1 )
            throws CertificateException
        {
        }

        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }

    }

    protected T behaviour;
    
    @Override
    @Before
    public void before()
        throws Exception
    {
        trustAllHttpsCertificates();
        super.before();
    }

    @Override
    public void configureProvider( ServerProvider provider )
    {
        super.configureProvider( provider );
        provider.addBehaviour( "/*", behaviour() );
        // ( (JettyServerProvider) provider() ).addDefaultServices();
    }

    protected abstract T behaviour();


    protected byte[] fetch( String url )
        throws IOException, MalformedURLException
    {
        InputStream in = new URL( url ).openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[16 * 1024];
        int count = -1;
        while ( ( count = in.read( b ) ) != -1 )
        {
            out.write( b, 0, count );
        }
        out.close();
        return out.toByteArray();
    }

    private static void trustAllHttpsCertificates()
    {
        SSLContext context;

        TrustManager[] _trustManagers = new TrustManager[] { new CustomTrustManager() };
        try
        {
            context = SSLContext.getInstance( "SSL" );
            context.init( null, _trustManagers, new SecureRandom() );
        }
        catch ( GeneralSecurityException gse )
        {
            throw new IllegalStateException( gse.getMessage() );
        }
        HttpsURLConnection.setDefaultSSLSocketFactory( context.getSocketFactory() );
    }
}