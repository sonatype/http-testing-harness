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
package org.sonatype.tests.http.server.api;

import java.io.File;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.Servlet;

public interface ServerProvider
{

  void start()
      throws Exception;

  void stop()
      throws Exception;

  boolean isStarted();

  /**
   * The URL of the server.
   */
  URL getUrl();

  /**
   * Add the given chain of Behaviour to execute for the given pathspec.
   *
   * @param pathspec e.g. "/path/*"
   */
  void addBehaviour(String pathspec, Behaviour... behaviour);

  /**
   * Adds the given servlet for the given pathspec.
   */
  void addServlet(String pathSpec, Servlet servlet);

  /**
   * Adds the given filter for the given pathspec.
   */
  void addFilter(String pathSpec, Filter filter);

  /**
   * Mounts the given file context for the given pathspec.
   */
  void serveFiles(String pathSpec, FileContext fileContext);

  /**
   * Set to 0 to auto-choose a free port.
   */
  void setPort(int port);

  int getPort();

  /**
   * @param keystore The keystore to use. (generated with e.g. 'keytool -keystore keystore -alias jetty -genkey
   *                 -keyalg DSA')
   */
  void setSSL(String keystore, String password);

  /**
   * Add authentication handler to the given pathspec.
   *
   * @param pathSpec e.g. "/path/*"
   * @param authName e.g. BASIC, DIGEST
   */
  void addAuthentication(String pathSpec, String authName);

  /**
   * Add the given user and password to the servers security realm. The password may be any type supported by the
   * authentication type (e.g. a certificate for client side certificate auth).
   */
  void addUser(String user, Object password);

  // void addFilter( String pathSpec, Filter filter );

  /**
   * @since 0.8
   */
  void setSSLTruststore(String truststore, String password);

  /**
   * @since 0.8
   */
  void setSSLNeedClientAuth(boolean needClientAuth);

  /**
   * File serving context.
   */
  public static class FileContext
  {
    private final boolean collectionAllow;

    private final File baseDir;

    public FileContext(final File baseDir) {
      this(baseDir, true);
    }

    public FileContext(final File baseDir, final boolean collectionAllow) {
      this.collectionAllow = collectionAllow;
      this.baseDir = baseDir;
    }

    public boolean isCollectionAllow() {
      return collectionAllow;
    }

    public File getBaseDir() {
      return baseDir;
    }
  }

}
