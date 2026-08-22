# Omnixys Context

Request-scoped context propagation for Spring Boot services: request id, correlation id, tenant, principal, client metadata, transport and trace metadata.

## Features

- `ContextSnapshot` bundling `requestId`, `correlationId`, `tenant`, `principal`, `client`, `transport`, `trace`
- `ContextAccessor` — thread-local holder for the current snapshot
- `ContextFilter` builds the snapshot from HTTP headers and populates SLF4J MDC
- `CorrelationIdFilter` propagates / generates `X-Correlation-Id` and echoes it on the response
- `HeaderTenantResolver` resolves a trusted tenant from `X-Tenant-Id`
- `DefaultClientMetadataResolver` maps IP / `User-Agent` / `X-Device-Id` / `Accept-Language`
- Pluggable `PrincipalResolver` to attach the authenticated principal (failures are non-fatal)
- Async propagation: `SnapshotContextAwareRunnable`, `SnapshotContextAwareCallable`, `SnapshotContextAwareSupplier`, `ContextAwareTaskDecorator`
- `ContextArgumentResolver` injects the current `ContextSnapshot` into MVC handler methods
- Spring Boot auto-configuration (`ContextAutoConfiguration`) with `@ConditionalOnMissingBean` overrides

## Installation

```xml
<dependency>
    <groupId>com.omnixys</groupId>
    <artifactId>context</artifactId>
    <version>1.0.2</version>
</dependency>
```

## Usage

Register the filters as beans (auto-configuration does this for you), then read the current snapshot:

```java
import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;

ContextSnapshot ctx = ContextAccessor.get();
String requestId = ctx.requestId();
String correlationId = ctx.correlationId();
TenantContext tenant = ctx.tenant();          // may be null
PrincipalContext principal = ctx.principal();  // may be null
ClientMetadata client = ctx.client();
```

Inject the snapshot directly into controller methods:

```java
@GetMapping("/orders")
public List<Order> orders(ContextSnapshot ctx) {
    return orderService.findByTenant(ctx.tenant().tenantId());
}
```

Propagate the snapshot into async work:

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
ContextSnapshot snapshot = ContextAccessor.get();
executor.execute(new SnapshotContextAwareRunnable(() -> {
    // ContextAccessor.get() returns the captured snapshot here
}));
```

Or decorate a Spring `ThreadPoolTaskExecutor` with `ContextAwareTaskDecorator` so every submitted task inherits the submitting thread's context.

## Headers

| Header | Used for |
| --- | --- |
| `X-Request-Id` | Request id; generated as UUID if absent |
| `X-Correlation-Id` | Correlation id; falls back to request id, else generated |
| `X-Tenant-Id` | Trusted tenant id |
| `X-Trace-Id` / `X-Span-Id` | Trace metadata |
| `User-Agent` / `X-Device-Id` / `Accept-Language` | Client metadata |

## Auto-configuration

`ContextAutoConfiguration` (web applications only) registers the filters, the argument resolver, the task decorator and a default `ThreadPoolTaskExecutor` named `taskExecutor`. Provide your own bean to override any default.

## Development

```bash
mvn test
```
