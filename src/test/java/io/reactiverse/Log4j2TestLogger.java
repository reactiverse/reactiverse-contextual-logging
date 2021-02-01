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


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Log4j2TestLogger implements TestLogger {

  private ByteArrayOutputStream baos;
  private Logger delegate;

  public Log4j2TestLogger(String pattern) {
    baos = new ByteArrayOutputStream();

    String id = UUID.randomUUID().toString();

    LoggerContext context = LoggerContext.getContext(false);
    Configuration config = context.getConfiguration();
    PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();
    Appender appender = OutputStreamAppender.createAppender(layout, null, baos, id, false, true);
    appender.start();
    config.addAppender(appender);

    AppenderRef ref = AppenderRef.createAppenderRef(id, null, null);
    LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.ALL, id, "false", new AppenderRef[]{ref}, null, config, null);
    loggerConfig.addAppender(appender, null, null);
    config.addLogger(id, loggerConfig);
    context.updateLoggers();

    delegate = LogManager.getLogger(id);
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
