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

import io.vertx.core.impl.ContextInternal;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

@Plugin(name = "VertxContextualLogging", category = PatternConverter.CATEGORY)
@ConverterKeys("vcl")
public class Log4j2Converter extends LogEventPatternConverter {

  private String key;
  private String defaultValue = "";

  private Log4j2Converter(String[] options) {
    super(options != null && options.length > 0 ? "vcl{" + options[0] + '}' : "vcl", "vcl");
    if (options != null && options.length > 0) {
      String option = options[0];
      int separator = option.indexOf(":-");
      if (separator == -1) {
        key = option;
      } else {
        key = option.substring(0, separator);
        defaultValue = option.substring(separator + 2);
      }
    }
  }

  @Override
  public void format(LogEvent event, StringBuilder toAppendTo) {
    ContextInternal context = ContextInternal.current();
    if (context != null && key != null) {
      toAppendTo.append(ContextualData.getOrDefault(key, defaultValue));
    } else {
      toAppendTo.append(defaultValue);
    }
  }

  public static Log4j2Converter newInstance(final String[] options) {
    return new Log4j2Converter(options);
  }
}
