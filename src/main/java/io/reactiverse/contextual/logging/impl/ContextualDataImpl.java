/*
 * Copyright 2024 Red Hat, Inc.
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

package io.reactiverse.contextual.logging.impl;

import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.reactiverse.contextual.logging.impl.ContextualDataStorage.CONTEXTUAL_DATA_KEY;
import static io.vertx.core.spi.context.storage.AccessMode.CONCURRENT;

public class ContextualDataImpl {

  private static final Logger log = LoggerFactory.getLogger(ContextualDataImpl.class);

  /**
   * Put a value in the contextual data map.
   *
   * @param key   the key of the data in the contextual data map
   * @param value the data value
   */
  public static void put(String key, String value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    ContextInternal ctx = ContextInternal.current();
    if (ctx == null) {
      if (log.isTraceEnabled()) {
        log.trace("Attempt to set contextual data from a non Vert.x thread", new Exception());
      }
    } else {
      contextualDataMap(ctx).put(key, value);
    }
  }

  /**
   * Get a value from the contextual data map.
   *
   * @param key the key of the data in the contextual data map
   * @return the value or null if absent or the method is invoked on a non Vert.x thread
   */
  public static String get(String key) {
    Objects.requireNonNull(key);
    ContextInternal ctx = ContextInternal.current();
    if (ctx != null) {
      return contextualDataMap(ctx).get(key);
    }
    return null;
  }

  /**
   * Get a value from the contextual data map.
   *
   * @param key          the key of the data in the contextual data map
   * @param defaultValue the value returned when the {@code key} is not present in the contextual data map or the method is invoked on a non Vert.x thread
   * @return the value or the {@code defaultValue} if absent or the method is invoked on a non Vert.x thread
   */
  public static String getOrDefault(String key, String defaultValue) {
    Objects.requireNonNull(key);
    ContextInternal ctx = ContextInternal.current();
    if (ctx != null) {
      return contextualDataMap(ctx).getOrDefault(key, defaultValue);
    }
    return defaultValue;
  }

  /**
   * Get all values from the contextual data map.
   *
   * @return the values or {@code null} if the method is invoked on a non Vert.x thread
   */
  public static Map<String, String> getAll() {
    ContextInternal ctx = ContextInternal.current();
    if (ctx != null) {
      return Collections.unmodifiableMap(new HashMap<>(contextualDataMap(ctx)));
    }
    return null;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static ConcurrentMap<String, String> contextualDataMap(ContextInternal ctx) {
    ConcurrentMap lcd = Objects.requireNonNull(ctx).getLocal(CONTEXTUAL_DATA_KEY, CONCURRENT, ConcurrentHashMap::new);
    return (ConcurrentMap<String, String>) lcd;
  }
}
