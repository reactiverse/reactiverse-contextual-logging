/*
 * Copyright 2019 Red Hat, Inc.
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


import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class JULTestLogger implements TestLogger {

  private ByteArrayOutputStream baos;
  private StreamHandler handler;
  private Logger delegate = Logger.getLogger("io.reactiverse.contextual");

  public JULTestLogger(String pattern) {
    baos = new ByteArrayOutputStream();
    // WARNING: the pattern is not compatible with the JUL formatter so this must be manually modified
    // WARNING: if the upstream test changes
    pattern = "%{requestId:-foobar}$s ### %5$s%n";
    delegate.setLevel(Level.FINEST);
    handler = new StreamHandler(baos, new JULContextualDataFormatter(pattern));
    delegate.addHandler(handler);
  }

  @Override
  public void log(String s) {
    delegate.info(s);
  }

  @Override
  public List<String> getOutput() {
    handler.flush();
    handler.close();
    return Arrays.asList(baos.toString().split("\\r?\\n"));
  }
}
