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

import io.vertx.core.impl.VertxBuilder;
import io.vertx.core.spi.VertxServiceProvider;
import io.vertx.core.spi.context.storage.ContextLocal;

import java.util.concurrent.ConcurrentMap;

/**
 * SPI Implementation for {@link ContextLocal} storage.
 */
public class ContextualDataStorage implements VertxServiceProvider {

  @SuppressWarnings("rawtypes")
  static ContextLocal<ConcurrentMap> CONTEXTUAL_DATA_KEY = ContextLocal.registerLocal(ConcurrentMap.class);

  @Override
  public void init(VertxBuilder builder) {
  }
}
