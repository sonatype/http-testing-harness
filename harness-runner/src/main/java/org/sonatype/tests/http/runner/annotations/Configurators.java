package org.sonatype.tests.http.runner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sonatype.tests.http.runner.api.SuiteConfigurator;

/**
 * The annotation to set used configurators directly.
 * 
 * @author Benjamin Hanzelmann
 * @see SuiteConfigurator
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Inherited
public @interface Configurators
{
    public Class<? extends SuiteConfigurator>[] value();
}