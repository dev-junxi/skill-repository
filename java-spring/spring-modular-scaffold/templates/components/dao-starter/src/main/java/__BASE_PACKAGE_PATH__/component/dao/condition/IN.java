package __BASE_PACKAGE__.component.dao.condition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import java.util.Collection;

/**
 * @author wanglinqiao
 * Date 2025/2/13
 * Time 11:14
 * @since 2.5.0
 */
public class IN<T, F> extends Condition<T, F> {
    private Collection<F> fieldValues;

    public IN(SFunction<T, F> field, Collection<F> fieldValues) {
        super(field, null);
        this.fieldValues = fieldValues;
    }

    public static <T, F> IN<T, F> of(SFunction<T, F> field, Collection<F> fieldValues) {
        return new IN<>(field, fieldValues);
    }

    @Override
    public LambdaQueryWrapper<T> assemble(LambdaQueryWrapper<T> queryWrapper) {
        return queryWrapper.in(field, fieldValues);
    }
}
