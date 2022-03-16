# p1k-Server framework

(still in developing, but working yet, tested on real projects)

Control multiple microservices over the same jvm. Load new elements and unload others in real-time without stopping your microservices. Also provides efficient communication between them.
Combine elements and their settings like a lego constructor and gain your unique solution!

This framework currently does not provide socket communications or any useful processing itself out of box. Use services, modules and extensions for add required implementation.

Missing/planned:
1. Upload to maven central
2. Add support of RMI and RPC for services out of box (to kernel)
3. Possibility to migrate services from one jvm to another, easily and safe
4. Develop more elements (services, modules and extensions) and so gain an easy and flexible tools for:
   1. Create microservices
   2. Develop RESTful and another APIs
   3. Monitoring resources
   4. Launch FTP server
   5. Launch web sites
   6. Develop a video game backend
   7. Develop another custom solutions...

Elements:
1. **Services** - main working elements. Each service implementation (Impl) is isolated from each other on class loaders level. But there is a way to direct communication between services via custom interfaces (Shared);
2. **Modules** - small replaceable elements. Is too similar to services. Are used to be loaded into a specific service;
3. **Extensions** - elements capable to manage whole server. Can be used, for example: setting up elements, monitoring, calculate metrics, display GUI, provide a control over network, or some innovations which can be used from other elements.

How to create a service impl
---
A service implementation contain the main business logic. This side (Impl) of the element will be loaded first (resolved last).

__x-service-impl-1.0.jar__ (or __x-service-impl.jar__, or just __x-service.jar__) in dir ./core/services/

• Type: Service
• Subtype: X
• Side: Impl

*MANIFEST.MF*
```manifest
Manifest-Version: 1.0

Name: Service-Impl (*)
Subtype: X (*)
Version: 1.0 (*)
Base-Package: com.example.server (*)
Shared-Subtype: X (specify a contract/interface will be used. It requeres for inter-element communication)
Lib-Impl-Jar: jar jar (include private libs by file name. If a parent class loader already contain a similar lib, that parent lib will be skipped and new lib will be used instead) 
Lib-Shared-Jar: jar jar (connect shared libs by file name)
Module-Shared-Subtype: A B C (connect module interfaces by Shared-Subtype)
Module-Shared-Jar: jar jar (connect module interfaces by file name)
Service-Shared-Subtype: A B C (connect service interfaces by Shared-Subtype)
Service-Shared-Jar: jar jar (connect service interfaces by file name)
Import-All-Packages: false (By default is false and will be loaded classes only from the Base-Package package)

```

...

How to create a service shared
---
This side (Shared) of the element type (Service) is a contract that describes format of Impl side and contains necessary classes and interfaces for inter-element communication.

__x-service-shared-1.0.jar__ (or __x-service-shared.jar__, or just __x-service.jar__) in dir ./core/services/

• Type: Service
• Subtype: X
• Side: Shared

*MANIFEST.MF*
```manifest
Manifest-Version: 1.0

Name: Service-Shared (*)
Subtype: X (*)
Version: 1.0 (*)
Base-Package: com.example.server (*)
Lib-Impl-Jar: jar jar (include private libs by file name. If a parent class loader already contain a similar lib, that parent lib will be skipped and new lib will be used instead) 
Lib-Shared-Jar: jar jar (connect shared libs by file name)
Module-Shared-Subtype: A B C (connect module interfaces by Shared-Subtype)
Module-Shared-Jar: jar jar (connect module interfaces by file name)
Service-Shared-Subtype: A B C (connect service interfaces by Shared-Subtype)
Service-Shared-Jar: jar jar (connect service interfaces by file name)

```

...
