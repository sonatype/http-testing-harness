package org.sonatype.tests.jetty.server.behaviour;

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

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;
import org.sonatype.tests.jetty.server.behaviour.filesystem.Get;
import org.sonatype.tests.jetty.server.util.FileUtil;

/**
 * @author Benjamin Hanzelmann
 *
 */
@RunWith( ConfigurationRunner.class )
public class FileServerTest
    extends BehaviourSuiteConfiguration
{

    @Override
    public Get behaviour()
    {
        return (Get) super.behaviour();
    }

    @Test
    public void testServe()
        throws IOException
    {
        File f = FileUtil.createTempFile( "foo" );

        behaviour().setPath( f.getParent() );

        String url = url( f.getName() );
        byte[] ba = fetch( url );

        Assert.assertArrayEquals( "foo".getBytes( "UTF-8" ), ba );

        f.delete();
    }

    @Override
    @Before
    public void before()
        throws Exception
    {
        behaviour( new Get() );
        super.before();
    }

}
