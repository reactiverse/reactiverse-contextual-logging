/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.reactiverse.contextual.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.OutputStreamAppender;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class LogbackTestLogger implements TestLogger {

  private static final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

  static {
    @SuppressWarnings("unchecked")
    Map<String, String> ruleRegistry = (Map<String, String>) loggerContext.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
    if (ruleRegistry == null) {
      ruleRegistry = new HashMap<>();
    }
    loggerContext.putObject(CoreConstants.PATTERN_RULE_REGISTRY, ruleRegistry);
    ruleRegistry.put("vcl", LogbackConverter.class.getName());
  }

  private final ByteArrayOutputStream baos;
  private final Logger delegate;

  public LogbackTestLogger(String pattern) {
    baos = new ByteArrayOutputStream();

    PatternLayoutEncoder ple = new PatternLayoutEncoder();
    ple.setContext(loggerContext);
    ple.setPattern(pattern);
    ple.start();

    OutputStreamAppender<ILoggingEvent> oa = new OutputStreamAppender<>();
    ple.setContext(loggerContext);
    oa.setEncoder(ple);
    oa.setOutputStream(baos);
    oa.start();

    delegate = (Logger) LoggerFactory.getLogger(UUID.randomUUID().toString());
    delegate.setLevel(Level.INFO);
    delegate.setAdditive(false);
    delegate.addAppender(oa);
  }

  @Override
  public void log(String s) {
    delegate.info(s);
  }

  @Override
  public List<String> getOutput() {
    return Arrays.asList(baos.toString().split("\\r?\\n"));
  }
}
