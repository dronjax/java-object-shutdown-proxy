package com.dronjax.java.objectshutdownproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SomeService implements SomeInterface, SomeAnnotatedInterface {

  private static final Logger LOGGER = LoggerFactory.getLogger(SomeService.class);

  private final SomeCallback someCallback;

  SomeService(final SomeCallback someCallback) {
    this.someCallback = someCallback;
  }

  @Override
  public void someNormalMethod() {
    try {
      LOGGER.info("[NORMAL] START");
      Thread.sleep(500L);
      LOGGER.info("[NORMAL] FINISH");
      someCallback.callback();
    } catch (final InterruptedException e) {
      LOGGER.info("[NORMAL] EXCEPTION");
    }
  }

  @Override
  public void someOtherMethod() {
    try {
      LOGGER.info("[OTHER] START");
      Thread.sleep(500L);
      LOGGER.info("[OTHER] FINISH");
      someCallback.callback();
    } catch (final InterruptedException e) {
      LOGGER.info("[OTHER] EXCEPTION");
    }
  }

  @Override
  public void someExceptionMethod() {
    try {
      LOGGER.info("[OTHER] START");
      Thread.sleep(500L);
      LOGGER.info("[OTHER] FINISH");
      throw new RuntimeException("EXCEPTION haha");
    } catch (final InterruptedException e) {
      LOGGER.info("[OTHER] EXCEPTION");
    } finally {
      someCallback.callback();
    }
  }
}
