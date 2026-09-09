package __BASE_PACKAGE__.component.dao.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

/**
 * @author wanglq
 * Date 2022/8/11
 * Time 11:13 上午
 */
public interface DaoWrapper<T> {
    /**
     * 生成QueryWrapper
     *
     * @return QueryWrapper
     */
    default QueryWrapper<T> qrWrapper() {
        return new QueryWrapper<>();
    }

    /**
     * 生成LambdaQueryWrapper
     *
     * @return LambdaQueryWrapper
     */
    default MslLambdaQueryWrapper<T> lQrWrapper() {
        return new MslLambdaQueryWrapper<>();
    }

    /**
     * 生成UpdateWrapper
     *
     * @return UpdateWrapper
     */
    default UpdateWrapper<T> udWrapper() {
        return new UpdateWrapper<>();
    }

    /**
     * 生成LambdaUpdateWrapper
     *
     * @return LambdaUpdateWrapper
     */
    default LambdaUpdateWrapper<T> lUdWrapper() {
        return new LambdaUpdateWrapper<>();
    }
}
