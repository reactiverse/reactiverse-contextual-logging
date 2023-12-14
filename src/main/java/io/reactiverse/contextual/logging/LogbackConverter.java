/*
 * Copyright 2023 Red Hat, Inc.
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

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.vertx.core.impl.ContextInternal;

import static ch.qos.logback.core.util.OptionHelper.extractDefaultReplacement;

/**
 * Contextual data converter for Logback.
 */
public class LogbackConverter extends ClassicConverter {

  private String key;
  private String defaultValue;

  public LogbackConverter() {
    reset();
  }

  private void reset() {
    key = null;
    defaultValue = "";
  }

  @Override
  public void start() {
    String[] keyInfo = extractDefaultReplacement(getFirstOption());
    key = keyInfo[0];
    if (keyInfo[1] != null) {
      defaultValue = keyInfo[1];
    }
    super.start();
  }

  @Override
  public String convert(ILoggingEvent event) {
    ContextInternal context = ContextInternal.current();
    if (context != null && key != null) {
      return ContextualData.getOrDefault(key, defaultValue);
    }
    return defaultValue;
  }

  @Override
  public void stop() {
    reset();
    super.stop();
  }
}
