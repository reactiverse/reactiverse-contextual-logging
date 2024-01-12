
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

import org.apache.logging.log4j.core.util.ContextDataProvider;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Supplies log4j2 log events with Vert.x Contextual Data.
 */
public class VertxContextDataProvider implements ContextDataProvider {

  private static final FrozenStringMap EMPTY_FROZEN_STRING_MAP = new FrozenStringMap(Collections.emptyMap());

  @Override
  public Map<String, String> supplyContextData() {
    Map<String, String> all = ContextualData.getAll();
    if (all != null) {
      return all;
    }
    return Collections.emptyMap();
  }

  @Override
  public StringMap supplyStringMap() {
    Map<String, String> all = ContextualData.getAll();
    if (all != null && !all.isEmpty()) {
      return new FrozenStringMap(all);
    }
    return EMPTY_FROZEN_STRING_MAP;
  }

  private static class FrozenStringMap implements StringMap {

    final Map<String, String> delegate;

    FrozenStringMap(Map<String, String> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void freeze() {
    }

    @Override
    public boolean isFrozen() {
      return true;
    }

    @Override
    public void putAll(ReadOnlyStringMap source) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void putValue(String key, Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void remove(String key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> toMap() {
      return new HashMap<>(delegate);
    }

    @Override
    public boolean containsKey(String key) {
      return delegate.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> void forEach(BiConsumer<String, ? super V> action) {
      Objects.requireNonNull(action);
      for (Map.Entry<String, String> entry : delegate.entrySet()) {
        action.accept(entry.getKey(), (V) entry.getValue());
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V, S> void forEach(TriConsumer<String, ? super V, S> action, S state) {
      Objects.requireNonNull(action);
      for (Map.Entry<String, String> entry : delegate.entrySet()) {
        action.accept(entry.getKey(), (V) entry.getValue(), state);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V getValue(String key) {
      return (V) delegate.get(key);
    }

    @Override
    public boolean isEmpty() {
      return delegate.isEmpty();
    }

    @Override
    public int size() {
      return delegate.size();
    }
  }
}
