package __BASE_PACKAGE__.component.dao.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * {@link Optional} 的补充工厂。
 *
 * <p>来源实现用的是一个自制的可序列化 Optional（`com.msl.common.base.Optional`），
 * 移植时改用 JDK 的 {@link Optional}——它是标准、无需额外依赖，且这些值只在进程内流转，
 * 用不上原版的 Serializable 特性。
 *
 * <p>唯一需要补的是 {@code ofEmpty}：语义是<b>空集合视同不存在</b>，JDK 没有对应方法。
 */
public final class Optionals {

    private Optionals() {}

    /** 集合为 null 或空时返回 empty */
    public static <E, T extends Collection<E>> Optional<T> ofEmpty(T value) {
        return value == null || value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    /** Map 为 null 或空时返回 empty */
    public static <K, V, T extends Map<K, V>> Optional<T> ofEmpty(T value) {
        return value == null || value.isEmpty() ? Optional.empty() : Optional.of(value);
    }
}
