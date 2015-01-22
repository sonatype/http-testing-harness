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

import java.util.LinkedList;
import java.util.List;

import org.sonatype.tests.http.runner.ConfigurationHelper;
import org.sonatype.tests.http.runner.SuiteConfiguration;
import org.sonatype.tests.http.runner.SuiteConfigurator;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Junit4 test runner supporting multiple server configurations for the test class. This runner can only execute tests
 * from classes that extend/implement {@link DefaultSuiteConfiguration} or {@link DefaultSuiteConfiguration}.
 * <p>
 * The runner provides annotations to configure the used configurations.
 * {@link org.sonatype.tests.http.runner.annotations.Configurators} takes {@link SuiteConfigurator} classes and
 * uses them to run every test method. Every entry in
 * {@link org.sonatype.tests.http.runner.annotations.ConfiguratorList} will be loaded as a resource. The runner
 * expects every line of the resource to be the class name of a SuiteConfigurator.
 * {@link org.sonatype.tests.http.runner.annotations.IgnoreConfigurators} may be used to to ignore
 * configurators from the list.
 * <p>
 * If none of the annotations are present, the runner tries to load a default list (
 * <code>DefaultSuiteConfigurators.list</code>).
 *
 * @author Benjamin Hanzelmann
 * @see SuiteConfigurator
 * @see DefaultSuiteConfiguration
 */
public class ConfigurationRunner
    extends BlockJUnit4ClassRunner
{

  private List<Class<? extends SuiteConfigurator>> defaultConfiguratorClasses;

  private List<SuiteConfigurator> configurators = new LinkedList<SuiteConfigurator>();

  public ConfigurationRunner(Class<?> klass)
      throws InitializationError
  {
    super(klass);
    if (!SuiteConfiguration.class.isAssignableFrom(klass)) {
      IllegalArgumentException error =
          new IllegalArgumentException("Can only run tests inheriting from SuiteConfiguration.");
      throw new InitializationError(error);
    }
  }

  @Override
  protected List<FrameworkMethod> computeTestMethods()
  {
    if (defaultConfiguratorClasses == null) {
      initDefaultConfiguratorClasses();
    }
    Class<?> testClass = getTestClass().getJavaClass();
    configurators = ConfigurationHelper.computeConfigurators(testClass);

    List<FrameworkMethod> methods = super.computeTestMethods();

    List<FrameworkMethod> cfgMethods = new LinkedList<FrameworkMethod>();
    for (FrameworkMethod method : methods) {
      for (SuiteConfigurator cfg : configurators) {
        cfgMethods.add(new ConfiguratorMethod(method.getMethod(), cfg));
      }
    }
    return cfgMethods;
  }

  /**
   * Load list of configurators from all resources named "SuiteConfigurator.list". (One full class name per line.)
   */
  private void initDefaultConfiguratorClasses()
  {
    defaultConfiguratorClasses = ConfigurationHelper.getDefaultConfiguratorClasses();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void validateInstanceMethods(List<Throwable> errors)
  {
    if (computeTestMethods().isEmpty() && configurators != null && configurators.isEmpty()) {
      String msg = "No SuiteConfigurator found to run the tests with.";
      errors.add(new Exception(msg));
    }
    super.validateInstanceMethods(errors);
  }

  @Override
  protected Statement methodInvoker(FrameworkMethod method, Object test)
  {
    SuiteConfiguration cfg = SuiteConfiguration.class.cast(test);
    ConfiguratorMethod cfgMethod = ConfiguratorMethod.class.cast(method);
    cfg.setConfigurator(cfgMethod.getConfigurator());

    return super.methodInvoker(method, test);
  }

}