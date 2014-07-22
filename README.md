<!--

    Copyright (c) 2010-2014 Sonatype, Inc. All rights reserved.

    This program is licensed to you under the Apache License Version 2.0,
    and you may not use this file except in compliance with the Apache License Version 2.0.
    You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

    Unless required by applicable law or agreed to in writing,
    software distributed under the Apache License Version 2.0 is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.

-->
# Testing Harness for http-based tests.

This library provides two main components:
 * server-provider allows easy HTTP server and HTTP Proxy setups to be used in tests
 * junit-runner makes JUnit able to execute same test code for
   many server configurations, executing test methods multiple times
   with different server fixtures.

Supported test libraries:

 * JUnit 4.11

ServerProviders:

 * Jetty 8.1.x

# Usage as "test" HTTP server

As stubbing/mocking HTTP server: just add following dependency to your POM:

```
  <dependency>
    <groupId>org.sonatype.http-testing-harness</groupId>
    <artifactId>server-provider</artifactId>
    <version>...</version>
  </dependncy>
```

And in your test you can either use `org.sonatype.tests.http.server.jetty.impl.JettyServerProvider` directly,
or use "fluent" API using `org.sonatype.tests.http.server.fluent.Server`.

# JUnit integration

To have more tighter JUnit integration, add following dependency to your POM (it will pull in server-provider too):

```
  <dependency>
    <groupId>org.sonatype.http-testing-harness</groupId>
    <artifactId>junit-runner</artifactId>
    <version>...</version>
  </dependncy>
```

Using this module, you can use provided annotations to create tests that are running against combinations
of server fixtures, and also it provides very simple JUnit Rules to manage Server lifecycle as JUnit
`ExternalResource`.

# Authors/contributors

Original author:

 * Benjamin Hanzelmann https://github.com/nabcos

Contributor:

 * Tamas Cservenak https://github.com/cstamas


=====

Have fun,
~t~
