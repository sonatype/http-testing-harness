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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
public class DeployTest
    extends AsyncConnectorSuiteConfiguration
{

    @Test
    public void testArtifactUpload()
        throws Exception
    {
        // provider().addBehaviour( "/*", new Debug() );
        addExpectation( "gid/aid/version/aid-version-classifier.extension", "artifact" );
        addExpectation( "gid/aid/version/aid-version-classifier.extension.sha1", sha1( "artifact" ) );
        addExpectation( "gid/aid/version/aid-version-classifier.extension.md5", md5( "artifact" ) );

        Artifact artifact = artifact( "artifact" );
        List<ArtifactUpload> uploads = Arrays.asList( new ArtifactUpload( artifact, artifact.getFile() ) );
        connector().put( uploads, null );

        assertExpectations();
    }

    @Test
    public void testArtifactFileUpload()
        throws Exception
    {
        provider().addBehaviour( "/*", new FileServer() );
    
        Artifact artifact = artifact( "artifact" );
        List<ArtifactUpload> uploads = Arrays.asList( new ArtifactUpload( artifact, artifact.getFile() ) );
        connector().put( uploads, null );
    
        URL url = new URL( url( "repo", "gid", "aid", "version", "aid-version-classifier.extension.sha1" ) );
        InputStream in = url.openStream();

        int count = -1;
        byte[] b = new byte[16000];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ( ( count = in.read( b ) ) != -1 )
        {
            out.write( b, 0, count );
        }
        out.close();
        byte[] readBytes = out.toByteArray();
        assertArrayEquals( sha1( "artifact" ).getBytes( "UTF-8" ), readBytes );

        // addExpectation( "gid/aid/version/aid-version-classifier.extension.sha1", sha1( "artifact" ) );
        // addExpectation( "gid/aid/version/aid-version-classifier.extension.md5", md5( "artifact" ) );
    }
}
