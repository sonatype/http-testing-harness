package org.sonatype.tests.jetty.server.behaviour.filesystem;

import java.io.File;

import org.sonatype.tests.server.api.Behaviour;

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
