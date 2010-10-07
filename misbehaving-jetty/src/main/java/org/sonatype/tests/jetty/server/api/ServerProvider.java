package org.sonatype.tests.jetty.server.api;

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
     * Set to -1 to auto-choose a free port.
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
     * Add the given user and password to the servers security realm.
     */
    void addUser( String user, String password );
}
