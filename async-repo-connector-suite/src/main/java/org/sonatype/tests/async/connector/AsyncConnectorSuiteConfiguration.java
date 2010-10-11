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

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.RecordingTransferListener;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.test.util.impl.StubMetadata;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;
import org.sonatype.tests.jetty.runner.SuiteConfiguration;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class AsyncConnectorSuiteConfiguration
    extends SuiteConfiguration
{

    protected Logger logger = LoggerFactory.getLogger( this.getClass() );

    private AsyncRepositoryConnectorFactory factory;

    private TestRepositorySystemSession session;

    private RemoteRepository repository;

    private Artifact artifact;

    private Metadata metadata;

    protected RecordingTransferListener transferListener;

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
        this.metadata =
            new StubMetadata( "gid", "aid", "version", "maven-metadata.xml", Metadata.Nature.RELEASE_OR_SNAPSHOT, null );

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

}
