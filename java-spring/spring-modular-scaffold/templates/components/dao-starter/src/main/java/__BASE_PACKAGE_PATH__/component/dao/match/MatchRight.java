package __BASE_PACKAGE__.component.dao.match;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * 右匹配
 * 示例：%aa
 *
 * @author wanglq
 * Date 2023/6/29
 * Time 14:22
 */
public class MatchRight extends Match {
    public MatchRight(String source) {
        super(source);
    }

    @Override
    public String getMatchSql() {
        return "%" + source;
    }

    @Override
    public <T> void doMatch(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column) {
        if (hasMatch()) {
            wrapper.likeLeft(column, source);
        }
    }
}
