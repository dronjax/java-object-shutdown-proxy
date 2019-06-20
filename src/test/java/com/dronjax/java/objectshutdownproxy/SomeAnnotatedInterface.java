package com.dronjax.java.objectshutdownproxy;

@CountForObjectShutdown
public interface SomeAnnotatedInterface {

  void someNormalMethod();

  void someOtherMethod();

  void someExceptionMethod();
}
