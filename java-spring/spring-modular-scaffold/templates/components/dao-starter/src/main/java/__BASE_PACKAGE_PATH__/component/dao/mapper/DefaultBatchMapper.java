package __BASE_PACKAGE__.component.dao.mapper;

import __BASE_PACKAGE__.component.dao.utils.SqlUtils;
import java.util.Collection;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * @author wanglq
 * Date 2024/4/16
 * Time 20:27
 */
@Mapper
public interface DefaultBatchMapper {
    /**
     * 批量保存
     *
     * @param ts 待保存的数据
     * @return 成功数
     */
    @InsertProvider(type = SqlUtils.class, method = "saveBatchList")
    <T> long saveBatchList(Collection<T> ts);

    /**
     * 批量更新
     *
     * @param ts 批量更新
     * @return 成功数
     */
    @UpdateProvider(type = SqlUtils.class, method = "updateBatchList")
    <T> long updateBatchList(Collection<T> ts);
}
