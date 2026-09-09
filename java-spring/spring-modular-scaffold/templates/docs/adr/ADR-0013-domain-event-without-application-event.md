# ADR-0013: 领域事件不继承 ApplicationEvent；禁止静态 EventManager

Status: Accepted (2026-09-08)

Context:
  参考实现里的事件基类继承 Spring 的 ApplicationEvent，并配了一个静态
  EventManager（public static ApplicationContext + 静态 publishEvent）。

Decision:
  `DomainEvent` 放在 `__ARTIFACT_PREFIX__-contract`，**刻意不继承 ApplicationEvent**。
  Spring 4.2 起支持发布任意 POJO 作为事件。

  不继承换来两件事：
  1. contract 保住零 Spring 依赖，事件基类才能放进契约包
  2. 未来拆服务时事件类可直接序列化进 MQ——ApplicationEvent 自带的
     source / timestamp 字段到那时是纯噪音，还得额外写转换

  **不采用静态 EventManager。** 三个问题：静态可变全局状态；测试里无法 mock；
  最要命的是**它会诱导人在 domain 层直接调 EventManager.publishEvent()**——
  那就把 domain 对 Spring 的依赖从后门放了进来，使 ADR-0003 与 ArchUnit R3
  形同虚设。

Consequences:
  - 一律用注入的 ApplicationEventPublisher，发布点在 application 层
  - 跨模块事件类定义在**发布方的 -api module**
  - 用 @TransactionalEventListener(AFTER_COMMIT) 监听；未来换 MQ 时监听方
    改注解、不改逻辑

Verify:
  mvn -pl tools/__ARTIFACT_PREFIX__-archunit test   # R7（contract 零 Spring）
  ContractTest.domain_event_records_occurred_at_without_spring
