package org.sonatype.tests.http.runner.junit;

/*
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
class DummyProvider
    implements ServerProvider
{

    public URL getUrl()
        throws MalformedURLException
    {
        return URI.create( "dummy://url" ).toURL();
    }

    public void stop()
        throws Exception
    {

    }

    public void addBehaviour( String pathspec, Behaviour... behaviour )
    {
    }

    public void start()
        throws Exception
    {

    }

    public void initServer()
        throws Exception
    {
    }

    public void setPort( int port )
    {
    }

    public int getPort()
    {
        return -1;
    }

    public void setSSL( String keystore, String password )
    {
    }

    public void addAuthentication( String pathSpec, String authName )
    {

    }

    public void addUser( String user, Object password )
    {

    }

}
