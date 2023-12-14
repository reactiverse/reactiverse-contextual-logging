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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * JUL Formatted that is able to process Vert.x Context data. Besides the format used in the parent class,
 * vert.x context variables can be referred by name too. For simplicity the default variables are also exposed
 * by name and cannot be overridden.
 *
 * {@inheritDoc}
 * @author Paulo Lopes
 */
public final class JULContextualDataFormatter extends Formatter {

  private static final String placeholderPrefix = "%{";
  private static final String placeholderSuffix = "}";
  private static final String defaultEmpty = "";

  private final String template;
  private final Date dat = new Date();

  private static final List<String> RESERVED = Arrays.asList("date", "source", "logger", "level", "message", "thrown");

  private final List<BiFunction<LogRecord, ContextInternal, Object>> resolvers = new ArrayList<>();

  public JULContextualDataFormatter() {
    this(LogManager.getLogManager().getProperty(JULContextualDataFormatter.class.getName() + ".format"));
  }

  JULContextualDataFormatter(String template) {
    if (template == null) {
      // default to the JDK default
      template = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";
    }

    // add the default resolvers
    // 1. date
    resolvers.add((record, ctx) -> {
      // with java 11 this should be replaced with the new time APIs
      dat.setTime(record.getMillis());
      return dat;
    });
    // 2. source
    resolvers.add((record, ctx) -> {
      String source;
      if (record.getSourceClassName() != null) {
        source = record.getSourceClassName();
        if (record.getSourceMethodName() != null) {
          source = source + " " + record.getSourceMethodName();
        }
      } else {
        source = record.getLoggerName();
      }
      return source;
    });
    // 3. logger
    resolvers.add((record, ctx) -> record.getLoggerName());
    // 4. level
    resolvers.add((record, ctx) -> record.getLevel().getLocalizedName());
    // 5. message
    resolvers.add((record, ctx) -> formatMessage(record));
    // 6. thrown
    resolvers.add((record, ctx) -> {
      String throwable = defaultEmpty;
      if (record.getThrown() != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        record.getThrown().printStackTrace(pw);
        pw.close();
        throwable = sw.toString();
      }
      return throwable;
    });

    this.template = parseStringValue(template);
  }

  private String parseStringValue(String template) {

    StringBuilder buf = new StringBuilder(template);

    int startIndex = template.indexOf(placeholderPrefix);
    while (startIndex != -1) {
      int endIndex = findPlaceholderEndIndex(buf, startIndex);
      if (endIndex != -1) {
        String placeholder = buf.substring(startIndex + placeholderPrefix.length(), endIndex);

        int index = RESERVED.indexOf(placeholder);

        if (index == -1) {
          // lookup default value
          int defIndex = placeholder.indexOf(":-");

          // need to lock to use inside lambda
          final String defValue;
          final String ctxKey;

          if (defIndex != -1) {
            ctxKey = placeholder.substring(0, defIndex);
            defValue = placeholder.substring(defIndex + 2);
          } else {
            defValue = defaultEmpty;
            ctxKey = placeholder;
          }

          // placeholder is not present so we need to compute it at runtime
          resolvers.add((record, ctx) -> {
            if (ctx != null) {
              return ContextualData.getOrDefault(ctxKey, defValue);
            } else {
              return defValue;
            }
          });
          index = resolvers.size();
        }

        String sIndex = "%" + index;
        buf.replace(startIndex, endIndex + placeholderSuffix.length(), sIndex);
        startIndex = buf.indexOf(placeholderPrefix, startIndex + sIndex.length());
      } else {
        startIndex = -1;
      }
    }

    // the final transformed template
    final String format = buf.toString();

    // validate that the format string is valid
    try {
      final Object[] args = new Object[resolvers.size()];
      // fill with dummy data
      args[0] = new Date();
      args[1] = "";
      args[2] = "";
      args[3] = "";
      args[4] = "";
      args[5] = null;

      // fill the custom resolvers
      for (int i = 6; i < args.length; i++) {
        args[i] = null;
      }

      // if the format fails the template is invalid. This is also the technique used in the JDK however
      // the JDK will silently fall back to the default format in this case it makes sense to abort as the
      // behavior would not match what the users desire.
      String.format(format, args);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("format string \"" + template + "\" is not valid.");
    }

    return format;
  }

  private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
    int index = startIndex + placeholderPrefix.length();
    int withinNestedPlaceholder = 0;
    while (index < buf.length()) {
      if (substringMatch(buf, index, placeholderSuffix)) {
        if (withinNestedPlaceholder > 0) {
          withinNestedPlaceholder--;
          index = index + placeholderPrefix.length() - 1;
        } else {
          return index;
        }
      } else if (substringMatch(buf, index, placeholderPrefix)) {
        withinNestedPlaceholder++;
        index = index + placeholderPrefix.length();
      } else {
        index++;
      }
    }
    return -1;
  }

  private static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
    for (int j = 0; j < substring.length(); j++) {
      int i = index + j;
      if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String format(LogRecord record) {
    final Object[] args = new Object[resolvers.size()];
    final ContextInternal context = ContextInternal.current();
    // process the placeholder values
    for (int i = 0; i < args.length; i++) {
      args[i] = resolvers.get(i).apply(record, context);
    }
    // format
    return String.format(template, args);
  }
}
