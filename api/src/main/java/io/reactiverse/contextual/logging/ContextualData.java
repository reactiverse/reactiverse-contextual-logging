/*
 * Copyright 2021 Red Hat, Inc.
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

import io.reactiverse.contextual.logging.impl.ContextualDataImpl;
import io.vertx.codegen.annotations.VertxGen;

import java.util.Map;

/**
 * Helper to store data in the local context.
 */
@VertxGen
public interface ContextualData {

  /**
   * Put a value in the contextual data map.
   *
   * @param key the key of the data in the contextual data map
   * @param value the data value
   */
  static void put(String key, String value) {
    ContextualDataImpl.put(key, value);
  }

  /**
   * Get a value from the contextual data map.
   *
   * @param key the key of the data in the contextual data map
   *
   * @return the value or null if absent or the method is invoked on a non Vert.x thread
   */
  static String get(String key) {
    return ContextualDataImpl.get(key);
  }

  /**
   * Get a value from the contextual data map.
   *
   * @param key the key of the data in the contextual data map
   * @param defaultValue the value returned when the {@code key} is not present in the contextual data map or the method is invoked on a non Vert.x thread
   *
   * @return the value or the {@code defaultValue} if absent or the method is invoked on a non Vert.x thread
   */
  static String getOrDefault(String key, String defaultValue) {
    return ContextualDataImpl.getOrDefault(key, defaultValue);
  }

  /**
   * Get all values from the contextual data map.
   *
   * @return the values or {@code null} if the method is invoked on a non Vert.x thread
   */
  static Map<String, String> getAll() {
    return ContextualDataImpl.getAll();
  }
}
