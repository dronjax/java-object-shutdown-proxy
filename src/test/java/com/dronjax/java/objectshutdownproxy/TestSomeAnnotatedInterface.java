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
public final class TestSomeAnnotatedInterface {

  private static final long SHUTDOWN_WAIT_INTERVAL_IN_MILLIS = 100L;
  private static final long DELAY_WAITING_EXECUTOR_SUBMISSION = 100L;

  @Mock
  private SomeCallback someCallback;

  private SomeAnnotatedInterface someAnnotatedInterface;
  private ShutdownHandler shutdownHandler;

  @BeforeMethod
  private void init() {
    BasicConfigurator.configure();
    MockitoAnnotations.initMocks(this);
    final ObjectShutdownProxyComponent<SomeAnnotatedInterface> objectShutdownProxyComponent =
        ObjectShutdownProxyComponent.coverAnnotatedClassOrMethod(
            new SomeService(someCallback),
            SomeAnnotatedInterface.class
        );
    someAnnotatedInterface = objectShutdownProxyComponent.getProxiedService();
    shutdownHandler = objectShutdownProxyComponent.getShutdownHandler();
  }

  public void testCallAllMethod() throws Exception {
    final int maxNumOfThreads = 3;
    final ExecutorService executorService = Executors.newFixedThreadPool(maxNumOfThreads);
    final SomeAnnotatedInterface someAnnotatedInterfaceFinal = this.someAnnotatedInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someAnnotatedInterfaceFinal::someNormalMethod, executorService);
    }
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someAnnotatedInterfaceFinal::someOtherMethod, executorService);
    }
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someAnnotatedInterfaceFinal::someExceptionMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    shutdownHandler.waitForShutdown(SHUTDOWN_WAIT_INTERVAL_IN_MILLIS);
    Mockito
        .verify(someCallback, Mockito.times(methodTotalCall * maxNumOfThreads))
        .callback();
  }

  public void testCallAnnotatedMethod() throws Exception {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final SomeAnnotatedInterface someAnnotatedInterfaceFinal = this.someAnnotatedInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someAnnotatedInterfaceFinal::someNormalMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    shutdownHandler.waitForShutdown(SHUTDOWN_WAIT_INTERVAL_IN_MILLIS);
    Mockito
        .verify(someCallback, Mockito.times(methodTotalCall))
        .callback();
  }

  public void testCallNonAnnotatedNormalMethod() throws Exception {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final SomeAnnotatedInterface someAnnotatedInterfaceFinal = someAnnotatedInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someAnnotatedInterfaceFinal::someOtherMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    shutdownHandler.waitForShutdown(SHUTDOWN_WAIT_INTERVAL_IN_MILLIS);
    Mockito
        .verify(someCallback, Mockito.times(methodTotalCall))
        .callback();
  }

  public void testCallNonAnnotatedExceptionMethod() throws Exception {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final SomeAnnotatedInterface someAnnotatedInterfaceFinal = someAnnotatedInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someAnnotatedInterfaceFinal::someExceptionMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    shutdownHandler.waitForShutdown(SHUTDOWN_WAIT_INTERVAL_IN_MILLIS);
    Mockito
        .verify(someCallback, Mockito.times(methodTotalCall))
        .callback();
  }
}
