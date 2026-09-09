package __BASE_PACKAGE__.component.dao.condition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * @author wanglinqiao
 * Date 2025/2/13
 * Time 11:06
 * @since 2.5.0
 */
public class EQ<T, F> extends Condition<T, F> {

    public EQ(SFunction<T, F> field, F fieldValue) {
        super(field, fieldValue);
    }

    /**
     * @since 2.5.1 增加判断 fieldValue 是否为 null, 为 null 时自动拼装 isNull 条件
     */
    @Override
    public LambdaQueryWrapper<T> assemble(LambdaQueryWrapper<T> queryWrapper) {
        if (fieldValue == null) {
            return queryWrapper.isNull(field);
        }
        return queryWrapper.eq(field, fieldValue);
    }

    public static <T, F> EQ<T, F> of(SFunction<T, F> field, F fieldValue) {
        return new EQ<>(field, fieldValue);
    }
}
