# 🚀 L-Boot Framework

*L-Boot* is a lightweight, custom-built Java Web Framework inspired by Spring Boot. It was developed to demystify the "magic" of enterprise frameworks by implementing *Inversion of Control (IoC), **Dependency Injection (DI), **Aspect-Oriented Programming (AOP), and a **RESTful Web Server* from the ground up using pure Java Reflection and the JDK's built-in HTTP server.

---

## 🏗 Project Architecture

L-Boot follows a modular architecture where the framework core is decoupled from the user's business logic.

| Module | package | Responsibility |
| :--- | :--- | :--- |
| *Annotations* | ⁠ org.likith.annotations ⁠ | Metadata markers that drive the framework logic (e.g., ⁠ @LAutowired ⁠, ⁠ @LValue ⁠). |
| *Core Engine* | ⁠ org.likith.core ⁠ | The "Brain." Handles the Bean lifecycle, Proxy creation, and the Web Server. |
| *Scanner* | ⁠ org.likith.scanner ⁠ | The "Eyes." Scans the classpath to find annotated classes. |
| *Web Routing* | ⁠ org.likith.core ⁠ | The "Dispatcher." Routes URLs to Controller methods and resolves parameters. |



---

## ✨ Key Features

### 1. Inversion of Control (IoC) & Dependency Injection
•⁠  ⁠*Automated Scanning*: Discovers all ⁠ @LComponent ⁠ and ⁠ @Lcontroller ⁠ classes in the base package.
•⁠  ⁠*Dependency Wiring*: Uses ⁠ @LAutowired ⁠ to inject beans into fields.
•⁠  ⁠*Primary Support*: Handles multiple implementations of an interface via ⁠ @LPrimary ⁠.
•⁠  ⁠*Factory Beans: Supports ⁠ @LConfiguration ⁠ and ⁠ @LBean ⁠ for managing third-party objects like **Gson*.

### 2. AOP-Based Cross-Cutting Concerns
•⁠  ⁠*JDK Dynamic Proxies*: Automatically wraps beans in a "Stunt Double" if they are marked with ⁠ @LLoggable ⁠.
•⁠  ⁠*Declarative Logging*: Tracks method execution time and entry/exit logs without polluting business logic.



### 3. RESTful Web Services
•⁠  ⁠*Dynamic Routing*: Supports regex-based path mapping (e.g., ⁠ /user/{id} ⁠) using ⁠ @LPathVariable ⁠.
•⁠  ⁠*JSON Integration: Uses **Google Gson* to automatically serialize return objects and de-serialize ⁠ @LRequestBody ⁠ payloads.
•⁠  ⁠*Configuration*: Injects properties from ⁠ application.properties ⁠ directly into fields using ⁠ @LValue ⁠.

### 4. Professional Startup UX
•⁠  ⁠*L-Banner*: Displays a custom ASCII banner upon startup.
•⁠  ⁠*Auto-Scan*: Using ⁠ LBoot.run(Main.class) ⁠ automatically detects the base package, removing the need for hardcoded strings.

---

## 🛠 File Inventory

### Core Engine
•⁠  ⁠⁠ LBootContext.java ⁠: Manages the ⁠ beanMap ⁠ and runs the initialization passes.
•⁠  ⁠⁠ LProxyFactory.java ⁠: Logic for creating JDK Dynamic Proxies for AOP.
•⁠  ⁠⁠ LWebServer.java ⁠: The HTTP server and request dispatcher.
•⁠  ⁠⁠ LPropertyLoader.java ⁠: Loads and stores ⁠ application.properties ⁠.

### Annotations
•⁠  ⁠*Stereotypes*: ⁠ @LComponent ⁠, ⁠ @Lcontroller ⁠, ⁠ @LConfiguration ⁠.
•⁠  ⁠*DI/Config*: ⁠ @LAutowired ⁠, ⁠ @LPrimary ⁠, ⁠ @LValue ⁠, ⁠ @LBean ⁠.
•⁠  ⁠*Web Mapping*: ⁠ @LGetMapping ⁠, ⁠ @LPostMapping ⁠, ⁠ @LPathVariable ⁠, ⁠ @LRequestBody ⁠.
•⁠  ⁠*AOP*: ⁠ @LLoggable ⁠.

---

## 🔄 The Initialization Lifecycle

When ⁠ LBoot.run(Main.class) ⁠ is called, the framework executes these steps:

1.  *Phase 1 (Scanning)*: Find all classes in the package.
2.  *Phase 2 (Instantiation)*: Create instances of all Components and Config beans.
3.  *Phase 3 (Property Loading)*: Load keys/values from ⁠ application.properties ⁠.
4.  *Phase 4 (Wiring)*: Inject dependencies (⁠ @LAutowired ⁠) and configurations (⁠ @LValue ⁠).
5.  *Phase 5 (Proxying)*: Wrap beans in AOP proxies if required.
6.  *Phase 6 (Deployment)*: Start the Web Server on the configured port.



---

## 🚧 Roadmap (Future Enhancements)

While L-Boot v1.0 is stable, the following features are planned for future versions:

•⁠  ⁠*Multi-threading*: Implement a ⁠ ThreadPoolExecutor ⁠ in ⁠ LWebServer ⁠ to handle concurrent requests.
•⁠  ⁠*Global Exception Handling*: Add ⁠ @LControllerAdvice ⁠ to catch and format errors as JSON.
•⁠  ⁠*Bean Lifecycle Hooks*: Support ⁠ @LPostConstruct ⁠ for code that must run after injection.
•⁠  ⁠*Interceptors*: Implement a pre/post-handle mechanism for authentication and security.

---

## 🚀 Getting Started

1.  Place your configuration in ⁠ src/main/resources/application.properties ⁠.
2.  Annotate your Main class with ⁠ @LSpringBootApplication ⁠ (planned) or simply call the run method:

