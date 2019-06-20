package com.dronjax.java.objectshutdownproxy;

public interface ShutdownHandler {

  void waitForShutdown() throws Exception;

  void waitForShutdown(
      long interval
  ) throws Exception;
}
