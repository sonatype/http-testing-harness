package org.sonatype.tests.async.connector;

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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.RecordingTransferListener;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;
import org.sonatype.tests.async.connector.behaviour.Upload;
import org.sonatype.tests.jetty.runner.SuiteConfiguration;
import org.sonatype.tests.jetty.server.behaviour.Expect;
import org.sonatype.tests.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
public class AsyncConnectorSuiteConfiguration
    extends SuiteConfiguration
{

    protected Logger logger = LoggerFactory.getLogger( this.getClass() );

    private AsyncRepositoryConnectorFactory factory;

    private TestRepositorySystemSession session;

    private RemoteRepository repository;

    private Artifact artifact;

    // private Metadata metadata;

    protected RecordingTransferListener transferListener;

    private List<Upload> expectations = new LinkedList<Upload>();

    protected Expect expect;

    @Override
    @Before
    public void before()
        throws Exception
    {
        super.before();

        this.factory = new AsyncRepositoryConnectorFactory( NullLogger.INSTANCE, new TestFileProcessor() );
        this.session = new TestRepositorySystemSession();
        this.repository = new RemoteRepository( "async-test-repo", "default", url( "repo" ) );
        
        this.artifact = new StubArtifact( "gid", "aid", "classifier", "extension", "version", null );
        // this.metadata =
        // new StubMetadata( "gid", "aid", "version", "maven-metadata.xml", Metadata.Nature.RELEASE_OR_SNAPSHOT, null );

        transferListener = new RecordingTransferListener();
        session.setTransferListener( transferListener );


    }

    protected RepositoryConnectorFactory factory()
    {
        return factory;
    }

    protected RepositoryConnector connector()
        throws NoRepositoryConnectorException
    {
        return factory().newInstance( session(), repository() );
    }

    /**
     * @return
     */
    protected RepositorySystemSession session()
    {
        return session;
    }

    /**
     * @return
     */
    protected RemoteRepository repository()
    {
        return repository;
    }

    protected Artifact artifact()
    {
        return artifact;
    }

    protected Artifact artifact( String content )
        throws IOException
    {
        return artifact().setFile( TestFileUtils.createTempFile( content ) );
    }

    protected String md5( String string )
        throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        String algo = "MD5";
        return digest( string, algo );
    }

    private String digest( String string, String algo )
        throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest digest = MessageDigest.getInstance( algo );
        byte[] bytes = digest.digest( string.getBytes( "UTF-8" ) );
        StringBuilder buffer = new StringBuilder( 64 );
    
        for ( int i = 0; i < bytes.length; i++ )
        {
            int b = bytes[i] & 0xFF;
            if ( b < 0x10 )
            {
                buffer.append( '0' );
            }
            buffer.append( Integer.toHexString( b ) );
        }
        return buffer.toString();
    }

    protected String sha1( String string )
        throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        return digest( string, "SHA-1" );
    }

    protected void assertExpectations()
    {
        expect.assertExpectations();
    }

    protected Expect addExpectation( String path, String content )
        throws Exception
    {
        byte[] bytes = content.getBytes( "UTF-8" );
        return addExpectation( path, bytes );
    }

    private Expect addExpectation( String path, byte[] content )
    {
        expect.addExpectation( path, content );
        return expect;
    }

    @Override
    protected void configureProvider( ServerProvider provider )
    {
        super.configureProvider( provider );
        expect = new Expect();
        provider.addBehaviour( "/repo", expect );
    }

}
