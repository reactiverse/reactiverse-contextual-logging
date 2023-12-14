
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

import io.reactiverse.contextual.logging.impl.ContextualDataImpl;
import io.vertx.core.impl.ContextInternal;
import org.apache.logging.log4j.core.util.ContextDataProvider;

import java.util.Collections;
import java.util.Map;

/**
 * Supplies log4j2 log events with Vert.x Contextual Data.
 */
public class VertxContextDataProvider implements ContextDataProvider {

  @Override
  public Map<String, String> supplyContextData() {
    ContextInternal ctx = ContextInternal.current();
    if (ctx != null) {
      return Collections.unmodifiableMap(ContextualDataImpl.contextualDataMap(ctx));
    }
    return Collections.emptyMap();
  }
}
