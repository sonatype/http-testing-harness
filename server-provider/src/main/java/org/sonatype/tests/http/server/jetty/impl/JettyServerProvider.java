/*
 * Copyright (c) 2010-2013 Sonatype, Inc. All rights reserved.
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.EnumSet;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.behaviour.Content;
import org.sonatype.tests.http.server.jetty.behaviour.Pause;
import org.sonatype.tests.http.server.jetty.behaviour.Redirect;
import org.sonatype.tests.http.server.jetty.behaviour.Stutter;
import org.sonatype.tests.http.server.jetty.behaviour.Truncate;
import org.sonatype.tests.http.server.jetty.util.FileUtil;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;

/**
 * @author Benjamin Hanzelmann
 */
public class JettyServerProvider
    implements ServerProvider
{

  protected Server server;

  protected int port = -1;

  protected boolean ssl;

  private final String host = "localhost"; // InetAddress.getLocalHost().getCanonicalHostName();

  private ServletContextHandler webappContext;

  private String sslKeystorePassword;

  private String sslKeystore;

  private String sslTruststore;

  private String sslTruststorePassword;

  private boolean sslNeedClientAuth;

  private ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();

  private HashLoginService loginService;

  private String authType;

  public JettyServerProvider()
  {
    super();
  }

  public void setSSL(String keystore, String password) {
    this.ssl = true;
    this.sslKeystore = keystore;
    this.sslKeystorePassword = password;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void initServer()
      throws Exception
  {
    server = createServer();
  }

  public boolean isStarted() {
    return server != null && server.isStarted();
  }

  /**
   * @since 0.8
   */
  public void setSSLTruststore(final String truststore, final String password) {
    this.sslTruststore = truststore;
    this.sslTruststorePassword = password;
  }

  /**
   * @since 0.8
   */
  public void setSSLNeedClientAuth(final boolean needClientAuth) {
    this.sslNeedClientAuth = needClientAuth;
  }

  public void getServer()
      throws Exception
  {
    if (server != null) {
      initServer();
    }
  }

  public Server createServer()
      throws URISyntaxException
  {
    Server s = new Server();

    Connector connector;
    if (ssl) {
      connector = sslConnector();
    }
    else {
      connector = connector();
    }

    s.setConnectors(new Connector[]{connector});

    initWebappContext(s);

    // addDefaultServices();

    return s;
  }

  public void addAuthentication(String pathSpec, String authName) {
    if (server == null) {
      try {
        initServer();
      }
      catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    initAuthentication(pathSpec, authName);
  }

  private void initAuthentication(String pathSpec, String authName) {
    authType = authName;
    Constraint constraint = new Constraint();
    if (authName == null) {
      authName = Constraint.__BASIC_AUTH;
    }
    constraint.setName(authName);

    constraint.setRoles(new String[]{"users"});
    constraint.setAuthenticate(true);

    ConstraintMapping cm = new ConstraintMapping();
    cm.setConstraint(constraint);
    cm.setPathSpec(pathSpec);

    securityHandler.setRealmName("Test Server");
    securityHandler.setAuthMethod(authName);
    securityHandler.setStrict(true);

    if (authName.endsWith("CERT")) {
      Connector[] connectors = server.getConnectors();
      for (Connector c : connectors) {
        if (c instanceof SslConnector) {
          SslConnector sslConnector = (SslConnector) c;
          sslConnector.setNeedClientAuth(true);
        }
        else {
          throw new UnsupportedOperationException("Cannot use Client Side Certificate Auth without SSL.");
        }
      }
    }

    securityHandler.setConstraintMappings(new ConstraintMapping[]{cm});
    loginService = new HashLoginService("Test Server");
    securityHandler.setLoginService(loginService);

    webappContext.setSecurityHandler(securityHandler);
  }

  /**
   * Add the given user to the LoginService. If the password object is a {@link CertificateHolder},
   * {@link #addCertificate(String, CertificateHolder)} is called. For any other class, the String representation of
   * the object is used as a password.
   *
   * @param user     the username, may not be {@code null}.
   * @param password The password to use, may not be {@code null}.
   */
  public void addUser(String user, Object password) {
    if (authType == null) {
      throw new IllegalStateException("no authentication method set.");
    }
    if (password instanceof CertificateHolder) {
      if (!authType.endsWith("CERT")) {
        throw new UnsupportedOperationException("Cannot add certificate with non-CERT-authentication");
      }
      try {
        addCertificate(user, (CertificateHolder) password);
      }
      catch (Exception e) {
        throw new IllegalStateException(e.getMessage(), e);
      }
    }
    else {
      loginService.putUser(user, new Password(password.toString()), new String[]{"users"});
    }
  }

  /**
   * Adds the given certificate to the keystore for use with AUTH-CERT.
   *
   * @param alias      The alias to use for the key in the keystore.
   * @param certHolder The key and certificate to use.
   */
  public void addCertificate(String alias, CertificateHolder certHolder)
      throws Exception
  {
    Connector[] connectors = server.getConnectors();
    for (Connector connector : connectors) {
      if (connector instanceof SslConnector) {
        SslConnector sslConnector = (SslConnector) connector;

        KeyManagerFactory keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        InputStream in = null;
        try {
          try {
            in = new FileInputStream(resourceFile(sslKeystore));
          }
          catch (Exception e) {
            in = new FileInputStream(sslKeystore);
          }
          KeyStore keystore = KeyStore.getInstance("JKS");
          keystore.load(in, sslKeystorePassword == null ? null
              : sslKeystorePassword.toString().toCharArray());
          keystore.setCertificateEntry(alias, certHolder.getCertificate());

          Certificate[] chain = certHolder.getChain();
          for (int i = 1; i < chain.length; i++) {
            keystore.setCertificateEntry(alias + "chain" + i, chain[i]);
          }

          // PrivateKey key = certHolder.getKey();
          // Certificate[] chain = new Certificate[] { certHolder.getCertificate() };
          // keystore.setEntry( alias, new PrivateKeyEntry( key, chain ),
          // new PasswordProtection( sslKeystorePassword.toCharArray() ) );
          keyManagerFactory.init(keystore, sslKeystorePassword == null ? null
              : sslKeystorePassword.toString().toCharArray());
          KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

          SSLContext context = SSLContext.getInstance("TLS");
          context.init(keyManagers, new TrustManager[]{new CustomTrustManager()}, null);
          sslConnector.setSslContext(context);

          if (certHolder.getCertificate() instanceof X509Certificate) {
            X509Certificate x509cert = (X509Certificate) certHolder.getCertificate();
            Principal principal = x509cert.getSubjectDN();
            if (principal == null) {
              principal = x509cert.getIssuerDN();
            }
            final String username = principal == null ? "clientcert" : principal.getName();

            final char[] credential = B64Code.encode(x509cert.getSignature());

            addUser(username, String.valueOf(credential));
          }
          else {
            throw new IllegalArgumentException("Unsupported Certificate Type (need X509Certificate): "
                + certHolder.getCertificate().getClass());
          }
        }
        finally {
          if (in != null) {
            in.close();
          }
        }
      }
    }
  }

  public void addDefaultServices() {
    addServlet("/error/*", new ErrorServlet());
    addBehaviour("/content/*", new Content());
    addBehaviour("/stutter/*", new Stutter());
    addBehaviour("/pause/*", new Pause(), new Content());
    addBehaviour("/truncate/*", new Truncate());
    addBehaviour("/timeout/*", new Pause());
    addBehaviour("/redirect/*", new Redirect(), new Content());
  }

  @Override
  public void addServlet(String pathSpec, Servlet servlet) {
    if (webappContext == null) {
      try {
        initServer();
      }
      catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    webappContext.getServletHandler().addServletWithMapping(new ServletHolder(servlet), pathSpec);
  }

  @Override
  public void addFilter(String pathSpec, Filter filter) {
    if (webappContext == null) {
      try {
        initServer();
      }
      catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    webappContext.getServletHandler().addFilterWithMapping(new FilterHolder(filter), pathSpec,
        EnumSet.of(DispatcherType.REQUEST));
  }

  @Override
  public void serveFiles(final String pathSpec, final FileContext fileContext) {
    if (webappContext == null) {
      try {
        initServer();
      }
      catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    final ServletHolder servletHolder = new ServletHolder(new DefaultServlet());
    servletHolder.setInitParameter("resourceBase", fileContext.getBaseDir().getAbsolutePath());
    servletHolder.setInitParameter("dirAllowed", String.valueOf(fileContext.isCollectionAllow()));
    servletHolder.setInitParameter("acceptRanges", Boolean.TRUE.toString());
    servletHolder.setInitParameter("pathInfoOnly", Boolean.TRUE.toString());

    webappContext.getServletHandler().addServletWithMapping(servletHolder, pathSpec);
  }

  protected void initWebappContext(Server s)
      throws URISyntaxException
  {
    this.webappContext = new ServletContextHandler();
    // webappContext.setConfigurations( new Configuration[] { new WebXmlConfiguration(). } );
    // webappContext.setContextPath( "/" );
    // webappContext.setWar( "resources" );
    // webappContext.setServletHandler( new ServletHandler() );
    webappContext.setContextPath("/");
    HandlerCollection handlers = new HandlerCollection();
    handlers.setHandlers(new Handler[]{webappContext, new DefaultHandler()});
    s.setHandler(handlers);
  }

  private String resourceFile(String resource)
      throws Exception
  {
    URL r = getClass().getResource("/" + resource);
    if (r == null) {
      throw new IllegalStateException("cannot find resource: " + resource);
    }
    if ("file".equals(r.getProtocol())) {
      return new File(new URI(r.toExternalForm())).getAbsolutePath();
    }
    else {
      InputStream in = null;
      FileOutputStream out = null;
      File target = FileUtil.createTempFile("");
      try {
        in = r.openStream();
        out = new FileOutputStream(target);
        int count = -1;
        byte[] buf = new byte[16000];
        while ((count = in.read(buf)) != -1) {
          out.write(buf, 0, count);
        }
      }
      finally {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }
      return target.getAbsolutePath();
    }
  }

  protected Connector connector() {
    Connector connector = new BlockingChannelConnector();
    connector.setHost(host);
    if (port != -1) {
      connector.setPort(port);
    }
    return connector;
  }

  protected Connector sslConnector() {
    SslSocketConnector connector = new SslSocketConnector();
    String keystore;
    try {
      keystore = resourceFile(sslKeystore);
    }
    catch (Exception e) {
      keystore = sslKeystore;
    }

    connector.setHost(host);
    if (port != -1) {
      connector.setPort(port);
    }

    connector.setKeystore(keystore);
    connector.setPassword(sslKeystorePassword);
    connector.setKeyPassword(sslKeystorePassword);

    if (sslTruststore != null) {
      String truststore;
      try {
        truststore = resourceFile(sslTruststore);
      }
      catch (Exception e) {
        truststore = sslTruststore;
      }

      connector.setTruststore(truststore);
      connector.setTrustPassword(sslTruststorePassword);
    }

    connector.setNeedClientAuth(sslNeedClientAuth);

    return connector;
  }

  public void start()
      throws Exception
  {
    if (server == null) {
      initServer();
    }
    server.start();

    int total = 0;
    synchronized (server) {
      while (total < 3000 && !server.isStarted()) {
        server.wait(10);
        total += 10;
      }

      // extra wait to stabilize tests - ports not opened sometimes
      server.wait(10);
    }

    if (!server.isStarted()) {
      throw new IllegalStateException("Server didn't start in: " + total + "ms.");
    }

    port = server.getConnectors()[0].getLocalPort();
  }

  public void addBehaviour(String pathspec, Behaviour... behaviour) {
    addServlet(pathspec, new BehaviourServlet(behaviour));
  }

  public void stop()
      throws Exception
  {
    server.stop();

    int total = 0;
    while (total < 3000 && server.isStarted()) {
      server.wait(10);
      total += 10;
    }

    if (server.isStarted()) {
      throw new IllegalStateException("Server didn't stop in: " + total + "ms.");
    }

  }

  public URL getUrl()
      throws MalformedURLException
  {
    String protocol;
    if (ssl) {
      protocol = "https";
    }
    else {
      protocol = "http";
    }

    return new URL(protocol, host, port, "");
  }

  public ServletContextHandler getWebappContext() {
    return webappContext;
  }

  public void setWebappContext(ServletContextHandler webappContext) {
    this.webappContext = webappContext;
  }

  public int getPort() {
    return port;
  }

  public ConstraintSecurityHandler getSecurityHandler() {
    return securityHandler;
  }

  public void setSecurityHandler(ConstraintSecurityHandler securityHandler) {
    this.securityHandler = securityHandler;
  }

  /**
   * @author Benjamin Hanzelmann
   */
  public static class CertificateHolder
  {

    private PrivateKey key;

    private Certificate[] chain;

    public Certificate getCertificate() {
      return chain[0];
    }

    public Certificate[] getChain() {
      return chain;
    }

    @Deprecated
    public CertificateHolder(PrivateKey key, Certificate certificate) {
      this.key = key;
      this.chain = new Certificate[]{certificate};
    }

    public CertificateHolder(Certificate[] chain) {
      this.chain = chain;
    }

    @Deprecated
    public PrivateKey getKey() {
      return key;
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
