package __BASE_PACKAGE__.contract.event;

import java.time.Instant;

/**
 * 领域事件基类。
 *
 * <p><b>刻意不继承 Spring 的 {@code ApplicationEvent}</b>（ADR-0013）。Spring 4.2 起支持发布
 * 任意 POJO 作为事件，不继承换来两件事：
 *
 * <ol>
 *   <li>本 module 保住零 Spring 依赖，事件基类才能放进契约包
 *   <li>未来拆服务时事件类可直接序列化进 MQ，不必剥掉 {@code source}/{@code timestamp} 字段
 * </ol>
 *
 * <p>发布一律用注入的 {@code ApplicationEventPublisher}，发布点在 application 层。
 * 禁止静态 EventManager——那会诱导 domain 层依赖 Spring，使 ArchUnit R3 形同虚设。
 */
public abstract class DomainEvent {

    private final Instant occurredAt = Instant.now();

    public Instant occurredAt() {
        return occurredAt;
    }
}
