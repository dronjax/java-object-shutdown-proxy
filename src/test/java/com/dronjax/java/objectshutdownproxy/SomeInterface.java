package com.dronjax.java.objectshutdownproxy;

public interface SomeInterface {

  @CountForObjectShutdown
  void someNormalMethod();

  void someOtherMethod();

  void someExceptionMethod();
}
