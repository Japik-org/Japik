# Japik framework

(still in developing, but working)

Control multiple microservices loaded into the same or defferent jvm. Load new elements and unload others in real-time without stopping your microservices. Also provides efficient communication between them.
Combine elements and their settings like a lego constructor and gain your unique solution!

This framework does not provide socket communications by it self. Use services, modules and extensions to add required implementation. For example, use RMIProtocolExtension to enable RMI support for communication between services, or implement your custom protocol.

Missing/planned:
1. Upload to maven central
2. Refactoring Logger
3. Possibility to migrate services from one jvm to another automatically, easily and safe
4. Develop more elements (services, modules and extensions) and so gain an easy and flexible tools for:
   1. Create microservices
   2. Develop APIs like RESTful
   3. Monitoring resources
   4. Launching FTP servers
   5. Launching web sites
   6. Develop a video game backend
   7. Develop another custom solutions...

Elements:
1. **Services** - main working elements. Each service implementation (Impl) is isolated from each other on class loaders level. But there is a way to direct communication between services via custom interfaces (Connections/Shared);
2. **Modules** - small replaceable elements. Is too similar to services. Are used to be loaded into a specific service;
3. **Extensions** - elements capable to manage whole server. Can be used, for example: setting up elements, monitoring, calculate metrics, display GUI, provide a control over network, or provide some innovations which can be used from other elements.

How to create a service impl
---
A service implementation contain the main business logic. This side (Impl) of the element will be loaded first (resolved last).

__x-service-impl-1.0.jar__ (or __x-service-impl.jar__, or just __x-service.jar__) in dir ./services/

• Type: Service
• Subtype: X
• Side: Impl

*MANIFEST.MF*  
(*) - required
```manifest
Manifest-Version: 1.0

Name: Service-Impl (*)
Subtype: X (*)
Version: 1.0 (*)
Base-Package: com.example.server (*)
Class: com.example.server.services.x.XService (is the service class for initialize)
Shared-Subtype: X (specify a contract/interface will be used. It requeres for inter-element communication)
Lib-Impl-Jar: jar jar (include private libs by file name. If a parent class loader already contain a similar lib, that parent lib will be skipped and new lib will be used instead) 
Lib-Shared-Jar: jar jar (connect shared libs by file name)
Module-Shared-Subtype: A B C (connect module interfaces by Shared-Subtype)
Module-Shared-Jar: jar jar (connect module interfaces by file name)
Service-Shared-Subtype: A B C (connect service interfaces by Shared-Subtype)
Service-Shared-Jar: jar jar (connect service interfaces by file name)
Import-All-Packages: false (By default is false and if is then only classes from the base package (Base-Package) will be loaded)

```

*Java*
```java
package com.example.japik.services.x;

public final class XService extends AService<IXServiceConnection> {
    ... // your custom variables
   
   // new connection will be created for establish intercommunication
   @Override
   public IXServiceConnection createServiceConnection(ServiceConnectionParams params) {
        return new XServiceConnection(this, params);
   }
   
   ... // your custom service implementation below
}
```

```java
package com.example.japik.services.x;

public final class XServiceConnection
        extends AServiceConnection<XService, IXServiceConnection>
        implements IXServiceConnection {
   ... // your custom variables

    public XServiceConnection(@NotNull XService service, ServiceConnectionParams params) {
        super(service, params);
    }
   
   ... // your custom connection implementation below
}
```

*Versioning*

(1.0): 
1 is the major version of contract/shared used. There are no way to load contract which have another major version. 0 is the version of impl part. There are no limits to add more sub-versions like 1.0.1.abc. If two or more elements found with the same type, subtype and major version, so the element of the greatest version will be loaded.

How to create a service shared
---
This side (Shared) of the element type (Service) is a contract that describes format of Impl side and contains necessary classes and interfaces for inter-element communication.

__x-service-shared-1.0.jar__ (or __x-service-shared.jar__, or just __x-service.jar__) in dir ./services/

• Type: Service
• Subtype: X
• Side: Shared

*MANIFEST.MF*  
(*) - required
```manifest
Manifest-Version: 1.0

Name: Service-Shared (*)
Subtype: X (*)
Version: 1.0 (*)
Base-Package: com.example.japik (*)
Lib-Impl-Jar: jar jar (include private libs by file name. If a parent class loader already contain a similar lib, that parent lib will be skipped and new lib will be used instead) 
Lib-Shared-Jar: jar jar (connect shared libs by file name)
Module-Shared-Subtype: A B C (connect module interfaces by Shared-Subtype)
Module-Shared-Jar: jar jar (connect module interfaces by file name)
Service-Shared-Subtype: A B C (connect service interfaces by Shared-Subtype)
Service-Shared-Jar: jar jar (connect service interfaces by file name)

```

*java*
```java
package com.example.japik.services.x.shared; // or com.example.server.services.x.connection

public interface IXServiceConnection extends IServiceConnection {
    ... // define your custom methods for intercommunication between services
}
```

*Versioning*

(1.0): 
1 is the major version of contract/shared. 0 is the minor version that does not affect impl part. There are no limits to add more sub-versions like 1.0.1.abc.

Live Cycling
---
(TODO: refactoring and simplifications needed)

*Live Cycle operation*

0. initializing java object
1. init() - initialize variables, settings, custom live cycling, etc (fill RAM)
2. start() - start threads, allow processing
3. stopSlow() and stopForce() - stop or pause processing
4. destroy() - release all resources (free RAM)

*Live cycle combination examples*

0. just destroy -> ...
1. init -> destroy -> ...
2. init -> start -> stop -> destroy -> ...
3. init -> start -> stop -> start -> ...
4. init -> destroy -> init -> ...

*java*

```java
import LiveCycleController;

class XElement extends AElement { // for example, XService extends AService<IXServiceConnection>
   @Override
   protected void initLiveCycleController(LiveCycleController liveCycleController) {
      liveCycleController.getInitImplQueue().put(new LiveCycleImplId(
              "init example", LiveCycleController.PRIORITY_HIGH
      ), () -> {
         // your custom init implementation
      });

      liveCycleController.getStartImplQueue().put(...); // no order is important
      liveCycleController.getStartImplQueue().putPriorityOrder(...); // preserve order #1
      liveCycleController.getStartImplQueue().putPriorityOrder(...); // preserve order #2

      liveCycleController.putPriorityOrder(new XElementLiveCycleImpl1()); // preserve order #3 (for start impl)
      liveCycleController.putPriorityOrder(new XElementLiveCycleImpl2()); // preserve order #4 (for start impl)
   }

   private class XElementLiveCycleImpl1 extends AShortLiveCycleImplId {
      @Override public void init() throws Throwable {
         // your custom initialization
      }

      @Override public void start() throws Throwable {
         // start your threads here
      }

      // optional
      @Override public void stopSlow() throws Throwable {
         // try to stop your threads, finish your working processes competently
      }

      @Override public void stopForce() {
         // force stop your threads
      }

      @Override public void destroy() {
         // release your resources
      }

      // optional
      @Override public void announceStop() {
         // prepare before stop
      }

      // optional
      @Override public boolean canBeStoppedSafe() {
         return true; // is ready to stop?
      }
   }

   private class XElementLiveCycleImpl2 extends EmptyLiveCycleImplId {
      // name is equals your class name
      // you can override all or nothing here
   }
}
```

Each live cycle implementation added to the controller will be sorted according to ```priority```:
* PRIORITY_HIGHEST = min
* PRIORITY_HIGH = -1
* PRIORITY_NORMAL = 0
* PRIORITY_LOW = 1
* PRIORITY_LOWER = max
* or custom integer number

Also it may be useful to use method putAutoPriorityOrder(...) to reserve order.

Note: you add implementations durring execution of another init implementation.

Name (```name```) for LiveCycleImpl are used for identify them. When any exception (Throwable) occurs, you will see that name in logs. Also, you can remove them from queue if you know names.
Successfully finished LiveCycleImpl will be removed from the queue. When any exception occurs during live cycle operation, it will not be removed from the live cycle queue. So you can change something and then try execute the same live cycle operation (init/start/stop/...) to successfully finish it.

When some live cycle operation fails, that element will be set to status ```BROKEN```. Be careful, is allowed to execute any live cycle operation while status is ```BROKEN```. So the best way to resolve that status is to stop and then, if still broken, destroy the element.
