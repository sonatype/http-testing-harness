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
package org.sonatype.tests.http.server.jetty.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class FileUtil
{

    private static final File TMP = new File( System.getProperty( "java.io.tmpdir" ), "http-tests-"
        + UUID.randomUUID().toString().substring( 0, 8 ) );

    static
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    delete( TMP );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        } );
    }

    public static File createTempFile( String contents )
        throws IOException
    {
        return createTempFile( contents.getBytes( "UTF-8" ), 1 );
    }

    public static File createTempFile( byte[] pattern, int repeat )
        throws IOException
    {
        mkdirs( TMP );
        File tmpFile = Files.createTempFile(TMP.toPath(), "tmpfile-", ".data").toFile();
        write( pattern, repeat, tmpFile );

        return tmpFile;
    }

    public static void write( String content, File file )
        throws IOException
    {
        try
        {
            write( content.getBytes( "UTF-8" ), 1, file );
        }
        catch ( UnsupportedEncodingException e )
        {
            // broken VM
            throw new IOException( e.getMessage() );
        }
    }

    public static void write( byte[] pattern, int repeat, File file )
        throws IOException
    {
        file.deleteOnExit();
        file.getParentFile().mkdirs();
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream( file );
            for ( int i = 0; i < repeat; i++ )
            {
                out.write( pattern );
            }
        }
        finally
        {
            close( out );
        }
    }

    public static long copy( File src, File target )
        throws IOException
    {
        RandomAccessFile in = null;
        RandomAccessFile out = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try
        {
            in = new RandomAccessFile( src, "r" );

            target.getParentFile().mkdirs();

            out = new RandomAccessFile( target, "rw" );

            out.setLength( 0 );
            long size = in.length();

            // copy large files in chunks to not run into Java Bug 4643189
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4643189
            // use even smaller chunks to work around bug with SMB shares
            // http://forums.sun.com/thread.jspa?threadID=439695
            long chunk = ( 64 * 1024 * 1024 ) - ( 32 * 1024 );

            inChannel = in.getChannel();
            outChannel = out.getChannel();

            int total = 0;
            do
            {
                total += inChannel.transferTo( total, chunk, outChannel );
            }
            while ( total < size );
            return total;
        }
        finally
        {
            close( inChannel );
            close( outChannel );
            close( in );
            close( out );
        }
    }

    private static void close( Closeable c )
        throws IOException
    {
        if ( c != null )
        {
            c.close();
        }
    }

    public static void delete( File file )
        throws IOException
    {
        if ( file == null )
        {
            return;
        }

        Collection<File> undeletables = new ArrayList<File>();

        delete( file, undeletables );

        if ( !undeletables.isEmpty() )
        {
            throw new IOException( "Failed to delete " + undeletables );
        }
    }

    private static void delete( File file, Collection<File> undeletables )
    {
        if ( file.isDirectory() )
        {
            for ( String child : file.list() )
            {
                delete( new File( file, child ), undeletables );
            }
        }

        if ( !file.delete() && file.exists() )
        {
            undeletables.add( file.getAbsoluteFile() );
        }
    }

    public static byte[] getContent( File file )
        throws IOException
    {
        RandomAccessFile in = null;
        try
        {
            in = new RandomAccessFile( file, "r" );
            byte[] actual = new byte[(int) in.length()];
            in.readFully( actual );
            return actual;
        }
        finally
        {
            close( in );
        }
    }

    public static boolean mkdirs( File directory )
    {
        if ( directory == null )
        {
            return false;
        }

        if ( directory.exists() )
        {
            return false;
        }
        if ( directory.mkdir() )
        {
            return true;
        }

        File canonDir = null;
        try
        {
            canonDir = directory.getCanonicalFile();
        }
        catch ( IOException e )
        {
            return false;
        }

        File parentDir = canonDir.getParentFile();
        return ( parentDir != null && ( mkdirs( parentDir ) || parentDir.exists() ) && canonDir.mkdir() );
    }

}
