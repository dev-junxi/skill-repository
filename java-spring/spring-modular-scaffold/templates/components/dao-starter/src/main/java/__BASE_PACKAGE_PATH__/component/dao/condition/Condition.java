package __BASE_PACKAGE__.component.dao.condition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wanglinqiao
 * Date 2025/2/13
 * Time 11:06
 * @since 2.5.0
 */
@Getter
@Setter
public abstract class Condition<T, F> {
    protected SFunction<T, F> field;
    protected F fieldValue;

    public Condition(SFunction<T, F> field, F fieldValue) {
        this.field = Objects.requireNonNull(field);
        this.fieldValue = fieldValue;
    }

    public abstract LambdaQueryWrapper<T> assemble(LambdaQueryWrapper<T> queryWrapper);
}
