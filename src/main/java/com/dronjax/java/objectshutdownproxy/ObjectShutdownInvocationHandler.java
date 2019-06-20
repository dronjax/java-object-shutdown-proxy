package com.dronjax.java.objectshutdownproxy;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

final class ObjectShutdownInvocationHandler<T> implements InvocationHandler, ShutdownHandler {

  private static final long DEFAULT_SHUTDOWN_WAIT_INTERVAL = 1000L;
  private static final long MAX_WAIT_INTERVAL = 60000L;

  private final Logger logger;
  private final T originalService;
  private final Map<String, AtomicLong> runningMethodCountMap;
  private final AtomicLong runningMethodCount;

  ObjectShutdownInvocationHandler(
      final T originalService,
      final Class clazzContract,
      final boolean ignoreAnnotation
  ) {
    this.logger = LoggerFactory.getLogger(
        String.format(
            "[%s][%s]",
            this.getClass().getSimpleName(),
            originalService.getClass().getSimpleName()
        )
    );
    this.originalService = originalService;
    final boolean hasGlobalAnnotation =
        ignoreAnnotation || null != clazzContract.getAnnotation(CountForObjectShutdown.class);

    final ImmutableMap.Builder<String, AtomicLong> methodCallCountMapBuilder = ImmutableMap.builder();
    for (final Method method : clazzContract.getMethods()) {
      if (ignoreAnnotation || hasGlobalAnnotation || null != method.getAnnotation(CountForObjectShutdown.class)) {
        methodCallCountMapBuilder.put(
            method.toString(),
            new AtomicLong(0)
        );
      }
    }
    this.runningMethodCountMap = methodCallCountMapBuilder.build();
    this.runningMethodCount = new AtomicLong(0);
  }

  @Override
  public Object invoke(
      final Object proxy,
      final Method method,
      final Object[] args
  ) throws Throwable {
    try {
      if (runningMethodCountMap.containsKey(method.toString())) {
        runningMethodCount.incrementAndGet();
        runningMethodCountMap.get(method.toString()).incrementAndGet();
      }
      return method.invoke(originalService, args);
    } finally {
      runningMethodCountMap.get(method.toString()).decrementAndGet();
      runningMethodCount.decrementAndGet();
    }
  }

  @Override
  public void waitForShutdown() throws Exception {
    waitForShutdown(DEFAULT_SHUTDOWN_WAIT_INTERVAL);
  }

  @Override
  public void waitForShutdown(final long interval) throws Exception {
    final long shutdownWaitInterval = Math.min(interval, MAX_WAIT_INTERVAL);
    while (runningMethodCount.get() != 0L) {
      logger.info("Waiting for objectshutdownproxy with total method count {}", runningMethodCount);
      final StringBuilder stringBuilder = new StringBuilder("Showing estimation of running methods:");
      runningMethodCountMap.forEach((k, v) ->
          stringBuilder.append(
              String.format(
                  "\n - Method %s has %d count",
                  k,
                  v.get()
              )
          )
      );
      logger.info(stringBuilder.toString());
      Thread.sleep(shutdownWaitInterval);
    }
  }
}
