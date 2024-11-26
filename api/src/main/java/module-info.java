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

module io.reactiverse.contextual.logging {

  requires io.vertx.core;
  requires io.vertx.core.logging;

  // Annotation processing

  requires static io.vertx.codegen.api;

  exports io.reactiverse.contextual.logging;

  provides io.vertx.core.spi.VertxServiceProvider with io.reactiverse.contextual.logging.impl.ContextualDataStorage;
}
