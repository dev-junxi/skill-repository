package __BASE_PACKAGE__.contract.response;

import java.util.List;

/**
 * 分页结果。
 *
 * <p>放 contract 而不是 dao 组件——它是<b>接口契约</b>的一部分（Controller 要返回它），
 * 不是持久化细节。放 dao 会导致 api module 为了引用它而依赖持久层。
 */
public record PageData<T>(List<T> records, long total, long current, long size) {

    public static <T> PageData<T> of(List<T> records, long total, long current, long size) {
        return new PageData<>(records, total, current, size);
    }

    public static <T> PageData<T> empty(long current, long size) {
        return new PageData<>(List.of(), 0, current, size);
    }
}
