/*
 * Copyright (C) 2017-2017 DataStax Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.guava;

import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.context.InternalDriverContext;
import com.datastax.oss.driver.internal.core.session.DefaultSession;
import com.datastax.oss.driver.internal.core.session.RequestHandler;
import com.datastax.oss.driver.internal.core.session.RequestProcessor;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CompletionStage;

public class GuavaRequestAsyncProcessor<T extends Request, U>
    implements RequestProcessor<T, ListenableFuture<U>> {

  private final RequestProcessor<T, CompletionStage<U>> subProcessor;

  private final GenericType resultType;

  public GuavaRequestAsyncProcessor(
      RequestProcessor<T, CompletionStage<U>> subProcessor, GenericType resultType) {
    this.subProcessor = subProcessor;
    this.resultType = resultType;
  }

  @Override
  public boolean canProcess(Request request) {
    return subProcessor.canProcess(request);
  }

  @Override
  public boolean canProcess(Request request, GenericType resultType) {
    return canProcess(request) && resultType.equals(this.resultType);
  }

  @Override
  public RequestHandler<T, ListenableFuture<U>> newHandler(
      T request, DefaultSession session, InternalDriverContext context, String sessionLogPrefix) {
    return new GuavaRequestHandler(
        subProcessor.newHandler(request, session, context, sessionLogPrefix));
  }

  class GuavaRequestHandler implements RequestHandler<T, ListenableFuture<U>> {

    private final RequestHandler<T, CompletionStage<U>> subHandler;

    GuavaRequestHandler(RequestHandler<T, CompletionStage<U>> subHandler) {
      this.subHandler = subHandler;
    }

    @Override
    public ListenableFuture<U> handle() {
      return ListenableFutureUtils.toListenableFuture(subHandler.handle());
    }
  }
}