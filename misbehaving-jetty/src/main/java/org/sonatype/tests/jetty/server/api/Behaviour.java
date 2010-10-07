package org.sonatype.tests.jetty.server.api;

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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Behaviour
{
    public enum Keys
    {
        CONTENT, STUTTER_MSGS, STUTTER_TIME, CONTENT_SIZE, TRUNCATE_MSG;
    }

    /**
     * Prepare the execution of the Behaviour (e.g. extract information from path).
     */
    void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception;

    /**
     * Execute the Behaviour (e.g. send data, redirect, sleep, ...).
     * 
     * @return <code>true</code> if execution of following behaviours should continue.
     */
    boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception;
}
