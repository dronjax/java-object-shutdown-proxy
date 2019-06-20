package com.dronjax.java.objectshutdownproxy;

import org.apache.log4j.BasicConfigurator;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Test(groups = "medium")
public final class TestSomeInterface {

  private static final long SHUTDOWN_WAIT_INTERVAL_IN_MILLIS = 100L;
  private static final long DELAY_WAITING_EXECUTOR_SUBMISSION = 100L;

  @Mock
  private SomeCallback someCallback;

  private SomeInterface someInterface;
  private ShutdownHandler shutdownHandler;

  @BeforeMethod
  private void init() {
    BasicConfigurator.configure();
    MockitoAnnotations.initMocks(this);
    final ObjectShutdownProxyComponent<SomeInterface> objectShutdownProxyComponent =
        ObjectShutdownProxyComponent.coverAnnotatedClassOrMethod(
            new SomeService(someCallback),
            SomeInterface.class
        );
    someInterface = objectShutdownProxyComponent.getProxiedService();
    shutdownHandler = objectShutdownProxyComponent.getShutdownHandler();
  }

  public void testCallAnnotatedMethod() throws Exception {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final SomeInterface someInterfaceFinal = someInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someInterfaceFinal::someNormalMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    shutdownHandler.waitForShutdown(SHUTDOWN_WAIT_INTERVAL_IN_MILLIS);
    Mockito
        .verify(someCallback, Mockito.times(methodTotalCall))
        .callback();
  }

  public void testCallNonAnnotatedMethod() throws Exception {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final SomeInterface someInterfaceFinal = someInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someInterfaceFinal::someOtherMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    shutdownHandler.waitForShutdown(SHUTDOWN_WAIT_INTERVAL_IN_MILLIS);
    Mockito
        .verify(someCallback, Mockito.never())
        .callback();
  }

  public void testCallNonAnnotatedNormalMethod() throws Exception {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final SomeInterface someInterfaceFinal = someInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someInterfaceFinal::someOtherMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    shutdownHandler.waitForShutdown(SHUTDOWN_WAIT_INTERVAL_IN_MILLIS);
    Mockito
        .verify(someCallback, Mockito.never())
        .callback();
  }

  public void testCallNonAnnotatedExceptionMethod() throws Exception {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final SomeInterface someInterfaceFinal = someInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someInterfaceFinal::someExceptionMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    shutdownHandler.waitForShutdown(SHUTDOWN_WAIT_INTERVAL_IN_MILLIS);
    Mockito
        .verify(someCallback, Mockito.never())
        .callback();
  }
}
