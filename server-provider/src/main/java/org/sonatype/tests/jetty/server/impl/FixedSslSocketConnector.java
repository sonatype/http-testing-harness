package org.sonatype.tests.jetty.server.impl;

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

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.resource.Resource;


/**
 * A SSL connector that doesn't leak file handles when loading the key stores (cf. JETTY-1292).
 */
class FixedSslSocketConnector
    extends SslSocketConnector
{
    private transient String _password;

    private transient String _keyPassword;

    private transient String _trustPassword;

    @Override
    public void setPassword( String password )
    {
        _password = password;
        super.setPassword( password );
    }

    @Override
    public void setTrustPassword( String password )
    {
        _trustPassword = password;
        super.setTrustPassword( password );
    }

    @Override
    public void setKeyPassword( String password )
    {
        _keyPassword = password;
        super.setKeyPassword( password );
    }

    @Override
    protected SSLServerSocketFactory createFactory()
        throws Exception
    {

        return getSslContext().getServerSocketFactory();
    }

    @Override
    protected SSLContext createSSLContext()
        throws Exception
    {
        String _protocol = getProtocol();
        String _provider = getProvider();
        String _secureRandomAlgorithm = getSecureRandomAlgorithm();
        String _sslKeyManagerFactoryAlgorithm = getSslKeyManagerFactoryAlgorithm();
        String _sslTrustManagerFactoryAlgorithm = getSslTrustManagerFactoryAlgorithm();

        String _keystore = getKeystore();
        String _keystoreType = getKeystoreType();
        String _truststore = getTruststore();
        String _truststoreType = getTruststoreType();

        if ( _truststore == null )
        {
            _truststore = _keystore;
            _truststoreType = _keystoreType;
        }

        KeyManager[] keyManagers = null;
        InputStream keystoreInputStream = null;
        if ( _keystore != null )
        {
            keystoreInputStream = Resource.newResource( _keystore ).getInputStream();
        }
        KeyStore keyStore = KeyStore.getInstance( _keystoreType );
        keyStore.load( keystoreInputStream, _password == null ? null : _password.toString().toCharArray() );
        close( keystoreInputStream );

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( _sslKeyManagerFactoryAlgorithm );
        keyManagerFactory.init( keyStore, _keyPassword == null ? null : _keyPassword.toString().toCharArray() );
        keyManagers = keyManagerFactory.getKeyManagers();

        TrustManager[] trustManagers = null;
        InputStream truststoreInputStream = null;
        if ( _truststore != null )
        {
            truststoreInputStream = Resource.newResource( _truststore ).getInputStream();
        }
        KeyStore trustStore = KeyStore.getInstance( _truststoreType );
        trustStore.load( truststoreInputStream, _trustPassword == null ? null : _trustPassword.toString().toCharArray() );
        close( truststoreInputStream );

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance( _sslTrustManagerFactoryAlgorithm );
        trustManagerFactory.init( trustStore );
        trustManagers = trustManagerFactory.getTrustManagers();

        SecureRandom secureRandom =
            _secureRandomAlgorithm == null ? null : SecureRandom.getInstance( _secureRandomAlgorithm );

        SSLContext context =
            _provider == null ? SSLContext.getInstance( _protocol ) : SSLContext.getInstance( _protocol, _provider );

        context.init( keyManagers, trustManagers, secureRandom );
        setSslContext( context );
        return context;
    }

    private void close( InputStream is )
    {
        if ( is != null )
        {
            try
            {
                is.close();
            }
            catch ( IOException e )
            {
                // we tried
            }
        }
    }
}
