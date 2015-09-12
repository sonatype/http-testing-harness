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
package org.sonatype.tests.http.server.jetty.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider.CertificateHolder;

import com.google.common.io.ByteStreams;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Benjamin Hanzelmann
 */
public class ClientSideCertTest
    extends TestSupport
{
  private File clientKeystore;

  @Before
  public void setup()
      throws URISyntaxException
  {
    clientKeystore = util.resolveFile("src/test/resources/client.keystore");
  }

  @Test
  public void testClientSideCertFail()
      throws Exception
  {
    ServerProvider p = new JettyServerProvider();
    p.setSSL("keystore", "password");
    p.addAuthentication("/*", Constraint.__CERT_AUTH2);

    p.start();

    String url = p.getUrl().toString() + "/test";
    HttpsURLConnection connection = null;

    InputStream is = null;

    try {
      connection = connect(url);
      is = connection.getInputStream();
      fail("expected exception");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
  }

  private HttpsURLConnection connect(String location)
      throws Exception
  {
    URL url = new URL(location);
    HttpsURLConnection connection;
    // Uncomment this in case server demands some unsafe operations
    // System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
    connection = (HttpsURLConnection) url.openConnection();

    // Accept all hostnames (
    connection.setHostnameVerifier(new HostnameVerifier()
    {
      public boolean verify(String arg0, SSLSession arg1) {
        return true;
      }
    });

    connection.setRequestMethod("GET");
    connection.setRequestProperty("Content-Type", "text/plain");

    SSLSocketFactory sslSocketFactory =
        getFactory(clientKeystore, "password", "client");

    connection.setSSLSocketFactory(sslSocketFactory);
    return connection;
  }

  @Test
  public void testClientSideCert()
      throws Exception
  {
    ServerProvider p = new JettyServerProvider();
    p.setSSL("keystore", "password");
    p.addAuthentication("/*", Constraint.__CERT_AUTH2);

    CertificateHolder cert = getCertificate("client", clientKeystore.getAbsolutePath(), "password");
    p.addUser("client", cert);
    ((JettyServerProvider) p).addDefaultServices();
    p.start();

    String url = p.getUrl().toString() + "/content/foo";
    HttpsURLConnection connection = null;

    InputStream is = null;

    InputStream content = null;
    try {
      connection = connect(url);

      // Process response
      is = connection.getInputStream();
      content = (InputStream) connection.getContent();
      Integer length = Integer.valueOf(connection.getHeaderField("Content-Length"));
      byte[] b = new byte[length];
      assertEquals(length.intValue(), ByteStreams.read(content, b, 0, length));

      assertEquals("foo", new String(b).trim());
    }
    finally {
      if (is != null) {
        is.close();
      }
      if (content != null) {
        content.close();
      }
    }
  }

  private CertificateHolder getCertificate(String alias, String keystorePath, String keystorePass)
      throws Exception
  {
    FileInputStream is = null;
    Certificate cert = null;
    try {
      is = new FileInputStream(new File(keystorePath));
      KeyStore keystore = KeyStore.getInstance("JKS");
      keystore.load(is, keystorePass == null ? null : keystorePass.toString().toCharArray());
      cert = keystore.getCertificate(alias);
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
    return new CertificateHolder(new Certificate[]{cert});
  }

  private static SSLSocketFactory getFactory(File pKeyFile, String pKeyPassword, String certAlias)
      throws Exception
  {
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
    KeyStore keyStore = KeyStore.getInstance("JKS");

    InputStream keyInput = new FileInputStream(pKeyFile);
    keyStore.load(keyInput, pKeyPassword.toCharArray());
    keyInput.close();
    keyManagerFactory.init(keyStore, pKeyPassword.toCharArray());

    // Replace the original KeyManagers with the AliasForcingKeyManager
    KeyManager[] kms = keyManagerFactory.getKeyManagers();
    for (int i = 0; i < kms.length; i++) {
      if (kms[i] instanceof X509KeyManager) {
        kms[i] = new AliasForcingKeyManager((X509KeyManager) kms[i], certAlias);
      }
    }

    TrustManager[] _trustManagers = new TrustManager[]{new CustomTrustManager()};
    SSLContext context;
    try {
      context = SSLContext.getInstance("TLS");
      context.init(kms, _trustManagers, new SecureRandom());
    }
    catch (GeneralSecurityException gse) {
      throw new IllegalStateException(gse.getMessage());
    }
    HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

    // context.init( kms, null, null );
    return context.getSocketFactory();
  }

  /*
   * This wrapper class overwrites the default behavior of a X509KeyManager and always render a specific certificate
   * whose alias matches that provided in the constructor
   */
  private static class AliasForcingKeyManager
      implements X509KeyManager
  {

    X509KeyManager baseKM = null;

    String alias = null;

    public AliasForcingKeyManager(X509KeyManager keyManager, String alias) {
      baseKM = keyManager;
      this.alias = alias;
    }

    /*
     * Always render the specific alias provided in the constructor
     */
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
      return alias;
    }

    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
      return baseKM.chooseServerAlias(keyType, issuers, socket);
    }

    public java.security.cert.X509Certificate[] getCertificateChain(String alias) {
      return baseKM.getCertificateChain(alias);
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
      return baseKM.getClientAliases(keyType, issuers);
    }

    public PrivateKey getPrivateKey(String alias) {
      return baseKM.getPrivateKey(alias);
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
      return baseKM.getServerAliases(keyType, issuers);
    }
  }

  public static final class CustomTrustManager
      implements X509TrustManager
  {

    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
        throws CertificateException
    {
    }

    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
        throws CertificateException
    {
    }

    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

  }
}
