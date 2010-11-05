package org.sonatype.tests.http.runner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sonatype.tests.http.runner.api.SuiteConfigurator;

/**
 * This annotation may be used to filter a configurator list.
 * 
 * @author Benjamin Hanzelmann
 * @see ConfiguratorList
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Inherited
public @interface IgnoreConfigurators
{
    public Class<? extends SuiteConfigurator>[] value();
}