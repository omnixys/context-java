# Omnixys Context

Request-scoped context propagation for Spring Boot services.

## Features

- ContextSnapshot with requestId, correlationId, tenant, principal, client, transport, trace metadata
- Header-based tenant resolution
- Principal extraction from security context
- Client metadata parsing (IP, user agent, device, location)
- Context filter for Servlet and GraphQL
- Spring Boot auto-configuration

## Installation

```xml
<dependency>
    <groupId>com.omnixys</groupId>
    <artifactId>context</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

```java
@Autowired
private ContextAccessor contextAccessor;

ContextSnapshot ctx = contextAccessor.current();
String requestId = ctx.requestId();
String correlationId = ctx.correlationId();
TenantContext tenant = ctx.tenant();
```
