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

package examples;

import io.reactiverse.contextual.logging.ContextualData;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class ContextualDataExamples {

  private Logger log;

  public void initial(Vertx vertx, HttpClient httpClient) {
    // tag::initial[]
    vertx.createHttpServer().requestHandler(req -> {
      String requestId = generateId(req);
      ContextualData.put("requestId", requestId);

      log.info("Received HTTP server request");

      // ... handle request
      httpClient.request(HttpMethod.GET, "/my-service")
        .compose(HttpClientRequest::send)
        .compose(HttpClientResponse::body)
        .onComplete(ar -> {

          // ... requestId is still present in contextual data map here
          log.info("Received HTTP client response");

        });
    }).listen(8080);
    // end::initial[]
  }

  private String generateId(HttpServerRequest req) {
    return null;
  }

  public void eventBusInterceptors(Vertx vertx) {
    // tag::eventBusInterceptors[]
    vertx.eventBus().addOutboundInterceptor(event -> {
      String requestId = ContextualData.get("requestId");
      if (requestId != null) {
        event.message().headers().add("requestId", requestId);
      }
      event.next();
    });

    vertx.eventBus().addInboundInterceptor(event -> {
      String requestId = event.message().headers().get("requestId");
      if (requestId != null) {
        ContextualData.put("requestId", requestId);
      }
      event.next();
    });
    // end::eventBusInterceptors[]
  }
}
