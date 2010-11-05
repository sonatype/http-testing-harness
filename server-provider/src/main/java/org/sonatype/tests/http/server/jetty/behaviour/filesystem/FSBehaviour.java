package org.sonatype.tests.http.server.jetty.behaviour.filesystem;

import java.io.File;

import org.sonatype.tests.http.server.api.Behaviour;

public abstract class FSBehaviour
    implements Behaviour
{

    protected final File fPath;

    public FSBehaviour( String path )
    {
        this( new File( path ) );
    }

    public FSBehaviour( File file )
    {
        this.fPath = file;
    }

    protected File fs( String path )
    {
        return new File( fPath, path );
    }

}
