package __BASE_PACKAGE__.component.dao.match;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * 模糊匹配
 * @author wanglq
 * Date 2023/6/29
 * Time 15:44
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Match {
    protected String source;

    public boolean hasMatch() {
        return StringUtils.isNotBlank(source);
    }

    public abstract String getMatchSql();

    public abstract <T> void doMatch(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column);
}
