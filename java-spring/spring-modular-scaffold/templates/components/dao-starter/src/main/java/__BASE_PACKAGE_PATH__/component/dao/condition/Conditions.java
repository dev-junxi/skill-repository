package __BASE_PACKAGE__.component.dao.condition;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import java.util.List;

/**
 * @author wanglinqiao
 * Date 2025/2/13
 * Time 11:06
 * @since 2.5.0
 */
@SuppressWarnings("all")
public class Conditions {
    public static <T, F> EQ<T, F> EQ(SFunction<T, F> field, F fieldValue) {
        return new EQ<>(field, fieldValue);
    }

    public static <T, F> IN<T, F> IN(SFunction<T, F> field, List<F> fieldValues) {
        return new IN<>(field, fieldValues);
    }
}
