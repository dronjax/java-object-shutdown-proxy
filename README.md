# java-object-shutdown-proxy
Create proxy for your java object to synchronously wait for all methods to finish calling.

## Background
For some services development, you might need to wait for a service to shutdown properly before doing some 
other process.

This library is to help with that, you can create a proxy of any java object and it will create a 
shutdown handler to give you control whether you want to wait for a service to shutdown or not.

## Usage
To use it, you just need to construct **ObjectShutdownProxyComponent** and pass the service 
and the contract you want to return.

You can also selectively mark the method you want to wait during shutdown using 
**CountForObjectShutdown** annotation.

Example:
```java
  // other codes
  final ObjectShutdownProxyComponent objectShutdownProxyComponent =
    ObjectShutdownProxyComponent.coverAllPublicClassMethod(
      constructService(),
      SomeService.class
    );
  final SomeService someService = objectShutdownProxyComponent.getProxiedService();
  final ShutdownHandler shutdownHandler = objectShutdownProxyComponent.getShutdownHandler();
  // other codes
```

You can utilize **ShutdownHandler** to wait for object shutdown.

## License
Please see the **LICENSE** and comply before using.
