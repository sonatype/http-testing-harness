package org.sonatype.tests.http.runner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation may be used to set a list of configurators. It loads the given resources and expects the file to
 * contain the class names of configurators, one per line.
 * 
 * @author Benjamin Hanzelmann
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Inherited
public @interface ConfiguratorList
{
    public String[] value();
}