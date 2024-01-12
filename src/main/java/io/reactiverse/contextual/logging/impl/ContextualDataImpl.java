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

package io.reactiverse.contextual.logging.impl;

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import static java.util.Spliterator.*;

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
      return immutableCopy(contextualDataMap(ctx));
    }
    return null;
  }

  private static Map<String, String> immutableCopy(ConcurrentMap<String, String> map) {
    // log4j2 context data provider needs the data ordered by key.
    // So, instead of using a hash map copy, return
    // 1. an empty map if there are no entries
    List<Entry<String, String>> entries = new ArrayList<>(map.entrySet());
    if (entries.isEmpty()) {
      return Collections.emptyMap();
    }
    // 2. a singleton map (obviously ordered!) if there is a single entry
    if (entries.size() == 1) {
      Entry<String, String> entry = entries.get(0);
      return Collections.singletonMap(entry.getKey(), entry.getValue());
    }
    // 3. a map backed by an ordered list if there are several entries
    // In most cases, it should be faster to use such a map instead of a hash map.
    // Indeed, usually there are few entries and traversing a small list is relatively cheap.
    entries.sort(Entry.comparingByKey());
    return new ImmutableMap(entries);
  }

  @SuppressWarnings("unchecked")
  private static ConcurrentMap<String, String> contextualDataMap(ContextInternal ctx) {
    ConcurrentMap<Object, Object> lcd = Objects.requireNonNull(ctx).localContextData();
    return (ConcurrentMap<String, String>) lcd.computeIfAbsent(ContextualDataImpl.class, k -> new ConcurrentHashMap<>());
  }

  private static class ImmutableMap extends AbstractMap<String, String> {

    final List<Entry<String, String>> entries;

    ImmutableMap(List<Entry<String, String>> entries) {
      this.entries = entries;
    }

    @Override
    public int size() {
      return entries.size();
    }

    @Override
    public boolean isEmpty() {
      return entries.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
      for (Entry<String, String> entry : entries) {
        if (entry.getValue().equals(value)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean containsKey(Object key) {
      for (Entry<String, String> entry : entries) {
        if (entry.getKey().equals(key)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String get(Object key) {
      for (Entry<String, String> entry : entries) {
        if (entry.getKey().equals(key)) {
          return entry.getKey();
        }
      }
      return null;
    }

    @Override
    public Set<String> keySet() {
      return new AbstractSet<String>() {
        @Override
        public Iterator<String> iterator() {
          Iterator<Entry<String, String>> iterator = entries.iterator();
          return new Iterator<String>() {
            @Override
            public boolean hasNext() {
              return iterator.hasNext();
            }

            @Override
            public String next() {
              return iterator.next().getKey();
            }
          };
        }

        @Override
        public int size() {
          return entries.size();
        }
      };
    }

    @Override
    public Collection<String> values() {
      return new AbstractList<String>() {
        @Override
        public String get(int index) {
          return entries.get(index).getValue();
        }

        @Override
        public int size() {
          return entries.size();
        }
      };
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
      return new ImmutableMapEntrySet(entries);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super String> action) {
      Objects.requireNonNull(action);
      for (Entry<String, String> entry : entries) {
        action.accept(entry.getKey(), entry.getValue());
      }
    }
  }

  private static class ImmutableMapEntrySet extends AbstractSet<Entry<String, String>> {

    final List<Entry<String, String>> entries;

    ImmutableMapEntrySet(final List<Entry<String, String>> entries) {
      this.entries = entries;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
      return entries.iterator();
    }

    @Override
    public Spliterator<Entry<String, String>> spliterator() {
      return Spliterators.spliterator(iterator(), size(), ORDERED | IMMUTABLE | SIZED | NONNULL | DISTINCT);
    }

    @Override
    public int size() {
      return entries.size();
    }

    @Override
    public boolean isEmpty() {
      return entries.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      if (o == null) {
        return false;
      }
      for (Entry<String, String> entry : entries) {
        if (entry.equals(o)) {
          return true;
        }
      }
      return false;
    }
  }
}
