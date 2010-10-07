package org.sonatype.tests.general.auth;

import org.mortbay.jetty.security.Constraint;
import org.sonatype.tests.jetty.server.suites.SimpleTestSuite;
import org.sonatype.tests.jetty.server.util.AuthSuiteConfigurator;

import com.ning.http.client.Realm;

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


/**
 * @author Benjamin Hanzelmann
 *
 */
public class BasicAuthSimpleHttpTest
    extends SimpleTestSuite
{

    public BasicAuthSimpleHttpTest()
    {
        super( new AuthSuiteConfigurator( Constraint.__BASIC_AUTH ) );
        Realm realm = new Realm.RealmBuilder().setPassword( "password" ).setPrincipal( "user" ).build();
        setAuthentication( realm );
    }

}
