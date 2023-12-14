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

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.netty.util.internal.StringUtil.NEWLINE;
import static java.util.stream.Collectors.*;

public class ContextualLoggingIT extends VertxTestBase {

  private static final String REQUEST_ID_HEADER = "x-request-id";

  private static final Logger log = LoggerFactory.getLogger(ContextualLoggingIT.class);

  private Path logFile;
  private WebClient webClient;
  private HttpServer server;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    logFile = Paths.get("target", ContextualLoggingIT.class.getSimpleName() + ".log");
    webClient = WebClient.create(vertx, new WebClientOptions().setDefaultPort(8080));
    server = vertx.createHttpServer()
      .requestHandler(req -> req.response().end("Hello!"));
    server.listen(8081, "127.0.0.1").toCompletionStage().toCompletableFuture().get();
  }

  @Override
  protected void tearDown() throws Exception {
    Files.deleteIfExists(logFile);
    if (webClient != null) {
      webClient.close();
    }
    if (server != null) {
      server.close().toCompletionStage().toCompletableFuture().get();
    }
    super.tearDown();
  }

  @Test
  public void testContextualLogging() {
    vertx.deployVerticle(new TestVerticle(), onSuccess(id -> {
      List<String> ids = IntStream.range(0, 10).mapToObj(i -> UUID.randomUUID().toString()).collect(toList());
      sendRequests(ids, onSuccess(v -> {
        try {
          verifyOutput(ids);
          testComplete();
        } catch (Throwable t) {
          fail(t);
        }
      }));
    }));
    await();
  }

  private void sendRequests(List<String> ids, Handler<AsyncResult<Void>> handler) {
    List<Future<?>> futures = ids.stream()
      .map(id -> webClient
        .get("/")
        .expect(ResponsePredicate.SC_OK)
        .putHeader(REQUEST_ID_HEADER, id).send())
      .collect(toList());
    Future.all(futures).<Void>mapEmpty().onComplete(handler);
  }

  private void verifyOutput(List<String> ids) throws IOException {
    List<String> output = Files.readAllLines(logFile);
    assertEquals("foobar ### Started!", output.get(0));
    Map<String, List<String>> allMessagesById = output.stream()
      .skip(1)
      .map(line -> line.split(" ### "))
      .peek(split -> assertEquals(split[0], split[2]))
      .collect(groupingBy(split -> split[0], mapping(split -> split[1], toList())));
    assertEquals(ids.size(), allMessagesById.size());
    assertTrue(ids.containsAll(allMessagesById.keySet()));
    List<String> expected = Stream.<String>builder()
      .add("Received HTTP request")
      .add("Timer fired")
      .add("Blocking task executed")
      .add("Received Web Client response")
      .build()
      .collect(toList());
    for (List<String> messages : allMessagesById.values()) {
      assertEquals(String.join(NEWLINE, output), expected, messages);
    }
  }

  private static class TestVerticle extends AbstractVerticle {

    private HttpRequest<String> request;
    private WebClient webClient;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
      webClient = WebClient.create(vertx);
      request = webClient.getAbs("http://127.0.0.1:8081").as(BodyCodec.string());

      vertx.createHttpServer()
        .requestHandler(req -> {

          String requestId = req.getHeader(REQUEST_ID_HEADER);
          ContextualData.put("requestId", requestId);
          log.info("Received HTTP request ### " + requestId);

          vertx.setTimer(50, l -> {

            log.info("Timer fired ### " + requestId);

            vertx.executeBlocking(() -> {

              log.info("Blocking task executed ### " + requestId);
              return null;

            }, false, bar -> {

              request.send(rar -> {

                log.info("Received Web Client response ### " + requestId);
                req.response().end();

              });

            });
          });

        }).listen(8080, lar -> {

          if (lar.succeeded()) {
            log.info("Started!");
            startPromise.complete();
          } else {
            startPromise.fail(lar.cause());
          }

        });
    }
  }
}
