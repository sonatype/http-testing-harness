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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.tests.async.connector.behaviour.Upload;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
public class DeployTest
    extends AsyncConnectorSuiteConfiguration
{

    private List<Upload> expectations = new LinkedList<Upload>();

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

    private String md5( String string )
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

    private String sha1( String string )
        throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        return digest( string, "SHA-1" );
    }

    private void assertExpectations()
    {
        for ( Upload u : expectations )
        {
            u.assertContent();
        }
    }

    private Upload addExpectation( String path, String content )
        throws Exception
    {
        byte[] bytes = content.getBytes( "UTF-8" );
        return addExpectation( path, bytes );
    }

    private Upload addExpectation( String path, byte[] content )
    {
        Upload upload = new Upload( content );
        provider().addBehaviour( "repo/" + path, upload );
        expectations.add( upload );
        return upload;
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
