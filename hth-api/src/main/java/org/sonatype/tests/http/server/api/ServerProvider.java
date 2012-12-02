/*
 * Copyright (c) 2010-2012 Sonatype, Inc. All rights reserved.
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

import java.net.MalformedURLException;
import java.net.URL;

public interface ServerProvider
{

    /**
     * @return the URL for the server
     */
    URL getUrl()
        throws MalformedURLException;

    void stop()
        throws Exception;

    /**
     * Add the given chain of Behaviour to execute for the given pathspec.
     * 
     * @param pathspec e.g. "/path/*"
     */
    void addBehaviour( String pathspec, Behaviour... behaviour );

    void start()
        throws Exception;

    /**
     * Configure the underlying server instance.
     */
    void initServer()
        throws Exception;

    /**
     * Set to 0 to auto-choose a free port.
     * 
     * @param port
     */
    void setPort( int port );

    int getPort();

    /**
     * @param keystore The keystore to use. (generated with e.g. 'keytool -keystore keystore -alias jetty -genkey
     *            -keyalg DSA')
     * @param password
     */
    void setSSL( String keystore, String password );

    /**
     * Add authentication handler to the given pathspec.
     * 
     * @param pathSpec e.g. "/path/*"
     * @param authName e.g. BASIC, DIGEST
     */
    void addAuthentication( String pathSpec, String authName );

    /**
     * Add the given user and password to the servers security realm. The password may be any type supported by the
     * authentication type (e.g. a certificate for client side certificate auth).
     */
    void addUser( String user, Object password );

    // void addFilter( String pathSpec, Filter filter );

    /*
     * Returns true if the server is currently running, false otherwise.
     * @since 0.6
     */
    boolean isStarted();
}
