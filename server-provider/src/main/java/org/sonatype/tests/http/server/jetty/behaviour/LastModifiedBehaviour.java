/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.tests.http.server.jetty.behaviour;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.tests.http.server.api.Behaviour;

/**
 * {@link Behaviour} implementation that overrides the Last-Modified response header, depending on the value
 * this instance has set.
 * 
 * @author cstamas
 * @since 0.8
 */
public class LastModifiedBehaviour
    implements Behaviour
{
    private Date lastModified;

    public LastModifiedBehaviour( final Date date )
    {
        setLastModified( date );
    }

    public void setLastModified( final Date when )
    {
        lastModified = when;
    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        if ( lastModified != null )
        {
            response.setDateHeader( "last-modified", lastModified.getTime() );
        }
        return true;
    }

}
