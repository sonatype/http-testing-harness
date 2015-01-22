/*
 * Copyright (c) 2010-2014 Sonatype, Inc. All rights reserved.
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
package org.sonatype.tests.http.runner.junit;

import java.lang.reflect.Method;

import org.sonatype.tests.http.runner.SuiteConfigurator;

import org.junit.runners.model.FrameworkMethod;

/**
 * @author Benjamin Hanzelmann
 */
public class ConfiguratorMethod
    extends FrameworkMethod
{

  private SuiteConfigurator configurator;

  public ConfiguratorMethod(Method method, SuiteConfigurator cfg)
  {
    super(method);
    this.configurator = cfg;
  }

  public SuiteConfigurator getConfigurator()
  {
    return configurator;
  }

  @Override
  public String getName()
  {
    return String.format("%s %s", super.getName(), configurator.getName());
  }

  @Override
  public String toString()
  {
    return getName();
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((configurator == null) ? 0 : configurator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ConfiguratorMethod other = (ConfiguratorMethod) obj;
    if (configurator == null) {
      if (other.configurator != null) {
        return false;
      }
    }
    else if (!configurator.equals(other.configurator)) {
      return false;
    }
    return true;
  }

}
