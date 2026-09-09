package __BASE_PACKAGE__.component.dao.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集合工具。
 *
 * <p>来源实现是 msl-common 的 {@code MslCollectionUtil}（一个几百行的大工具类）。移植时
 * <b>只实现 dao 层实际用到的三个方法</b>，不整包搬运——把用不上的代码拖进来只会增加
 * 维护面和依赖（原版的 join 还依赖那个自制 Optional 的链式 API）。
 *
 * <p>需要更多集合工具时优先看 Guava 或 Stream API，确认没有再往这里加。
 */
public final class DaoCollections {

    private DaoCollections() {}

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }

    /** 集合元素经 converter 转换后收集成 List；空集合返回空 List 而非 null */
    public static <T, R> List<R> toList(Collection<T> list, Function<? super T, ? extends R> converter) {
        return isEmpty(list) ? new ArrayList<>() : list.stream().map(converter).collect(Collectors.toList());
    }

    /** 把集合按 per 个一组切分；空集合返回空 List */
    public static <T> List<List<T>> split(Collection<T> list, int per) {
        List<List<T>> result = new ArrayList<>();
        if (isEmpty(list) || per <= 0) {
            return result;
        }
        List<T> current = new ArrayList<>(per);
        for (T item : list) {
            current.add(item);
            if (current.size() == per) {
                result.add(current);
                current = new ArrayList<>(per);
            }
        }
        if (!current.isEmpty()) {
            result.add(current);
        }
        return result;
    }

    /** 元素经 getter 取字符串后用 delimiter 连接；空集合返回 {@link Optional#empty()} */
    public static <T> Optional<String> join(Collection<T> list, Function<? super T, String> getter, String delimiter) {
        if (isEmpty(list)) {
            return Optional.empty();
        }
        return Optional.of(list.stream().map(getter).collect(Collectors.joining(delimiter)));
    }
}
