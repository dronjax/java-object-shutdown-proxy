package com.dronjax.java.objectshutdownproxy;

import java.lang.reflect.Proxy;

public final class ObjectShutdownProxyComponent<T> {

  private final T service;
  private final ObjectShutdownInvocationHandler invocationHandler;

  private ObjectShutdownProxyComponent(
      final T service,
      final Class<T> clazzContract,
      final boolean ignoreAnnotation
  ) {
    this.invocationHandler = new ObjectShutdownInvocationHandler<>(
        service,
        clazzContract,
        ignoreAnnotation
    );
    this.service = createProxy(
        service,
        clazzContract
    );
  }

  public static <T> ObjectShutdownProxyComponent<T> coverAllPublicClassMethod(
      final T service,
      final Class<T> clazzContract
  ) {
    return new ObjectShutdownProxyComponent<>(
        service,
        clazzContract,
        true
    );
  }

  public static <T> ObjectShutdownProxyComponent<T> coverAnnotatedClassOrMethod(
      final T service,
      final Class<T> clazzContract
  ) {
    return new ObjectShutdownProxyComponent<>(
        service,
        clazzContract,
        false
    );
  }

  public T getProxiedService() {
    return service;
  }

  public ShutdownHandler getShutdownHandler() {
    return invocationHandler;
  }

  @SuppressWarnings("unchecked")
  private T createProxy(
      final T service,
      final Class<T> clazzContract
  ) {
    return (T) Proxy.newProxyInstance(
        service.getClass().getClassLoader(),
        new Class[]{clazzContract},
        invocationHandler
    );
  }
}
