# ADR-0004: Spring MVC（Servlet 栈）+ 虚拟线程，不用 WebFlux

Status: Accepted (2026-09-08)

Context:
  需要支持 SSE 流式输出。参考实现（Rust）全链路 async 是因为 Tokio 世界里没有别的选择。

Decision:
  用 `spring-boot-starter-web`（Servlet / MVC 栈）+ JDK 21 虚拟线程
  （`spring.threads.virtual.enabled=true`）。不用 WebFlux。

  理由：为了 SSE 把整个代码库染成响应式（Mono/Flux 穿透到 domain 层）是笔亏本买卖。
  SseEmitter + 虚拟线程足够扛，且领域模型能保持是普通对象。

Consequences:
  - ⚠️ **JDK 21 下 synchronized 块内阻塞会 pin 住载体线程**（JDK 24 才修），
    而 HikariCP 与 MyBatis 内部都有 synchronized。所以"开了虚拟线程就无限并发"
    是错的，**连接池仍是并发上限**。不写这条，将来一定有人按"虚拟线程很便宜"
    的直觉去调参然后翻车
  - MUST server.shutdown=graceful，否则滚动发布会砍断 SSE 长连接

Verify:
  grep -n "virtual" server/__ARTIFACT_PREFIX__-app/src/main/resources/application.yml
  AppSmokeTest.virtual_threads_are_enabled / graceful_shutdown_is_configured
