package com.dronjax.java.objectshutdownproxy;

import org.apache.log4j.BasicConfigurator;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Test(groups = "medium")
public final class TestSomeInterfaceIgnoreAnnotationLongWait {

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
        ObjectShutdownProxyComponent.coverAllPublicClassMethod(
            new SomeService(someCallback),
            SomeInterface.class
        );
    someInterface = objectShutdownProxyComponent.getProxiedService();
    shutdownHandler = objectShutdownProxyComponent.getShutdownHandler();
  }

  public void testCallAllMethod_ShutdownIntervalLongerThanMax() throws Exception {
    final int maxNumOfThreads = 3;
    final ExecutorService executorService = Executors.newFixedThreadPool(maxNumOfThreads);
    final SomeInterface someInterfaceFinal = someInterface;
    final int methodTotalCall = 10;
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someInterfaceFinal::someNormalMethod, executorService);
    }
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someInterfaceFinal::someOtherMethod, executorService);
    }
    for (int i = 0; i < methodTotalCall; ++i) {
      CompletableFuture.runAsync(someInterfaceFinal::someExceptionMethod, executorService);
    }

    Thread.sleep(DELAY_WAITING_EXECUTOR_SUBMISSION);
    final long startMillis = System.currentTimeMillis();
    shutdownHandler.waitForShutdown(120000L);
    final long endMillis = System.currentTimeMillis();

    Assert.assertTrue(endMillis - startMillis < 120000L);

    Mockito
        .verify(someCallback, Mockito.times(methodTotalCall * maxNumOfThreads))
        .callback();
  }
}
