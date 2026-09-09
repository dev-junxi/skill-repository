package __BASE_PACKAGE__.component.dao.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import __BASE_PACKAGE__.component.dao.condition.Condition;
import __BASE_PACKAGE__.component.dao.service.BaseService;
import com.msl.model.utils.HasKey;
import com.msl.view.Include;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author wanglq
 * Date 2022/9/20
 * Time 下午3:20
 */
public interface BaseDao<M extends BaseMapper<T>, T extends HasKey<K>, K extends Serializable> {
    /**
     * 根据key集合查询数据
     *
     * @param keys key集合
     * @return map数据集合，key为key,value为实体
     */
    Map<K, T> listMapByKeys(Collection<K> keys);

    /**
     * 查询所有数据
     *
     * @return 数据集合
     */
    List<T> listAll();

    /**
     * 根据key集合查询数据
     *
     * @param keys key集合
     * @return 数据集合
     */
    List<T> listByKeys(Collection<K> keys);

    /**
     * 根据某字段查询列表数据
     * 字段值为null时不进行查询
     *
     * @param field      指定字段
     * @param fieldValue 指定字段值
     * @return 实体集合
     * @since 2.1.0
     */
    <F> List<T> listByField(SFunction<T, F> field, F fieldValue);

    /**
     * 根据某字段值集合批量查询列表数据
     * 字段值集合为空时不进行查询
     *
     * @param field       指定字段
     * @param fieldValues 指定字段值集合
     * @return 实体集合
     * @since 2.1.2
     */
    <F> List<T> listInField(SFunction<T, F> field, Collection<F> fieldValues);

    /**
     * 根据某字段查询单条数据,存在多条数据时抛出异常
     * 字段值为null时不进行查询
     *
     * @param field      指定字段
     * @param fieldValue 指定字段值
     * @return 实体
     * @since 2.1.0
     */
    <F> Optional<T> getOneByField(SFunction<T, F> field, F fieldValue);

    /**
     * 根据某字段查询单条数据，自动拼接 limit 1
     * 字段值为null时不进行查询
     *
     * @param field      指定字段
     * @param fieldValue 指定字段值
     * @return 实体
     * @since 2.1.0
     */
    <F> Optional<T> getLimitOneByField(SFunction<T, F> field, F fieldValue);

    /**
     * 根据key获取实体数据
     *
     * @param key key
     * @return 实体
     */
    Optional<T> getEntityByKey(K key);

    /**
     * 保存实体
     *
     * @param t 实体
     * @return 是否保存成功
     */
    boolean saveEntity(T t);

    /**
     * 根据key更新实体
     *
     * @param t 实体
     * @return 是否保存成功
     */
    boolean updateEntityByKey(T t);

    /**
     * 批量保存实体
     * 此方法直接调用 Mybatis-Plus 自带的批量保存，实际进行的循环操作
     *
     * @param ts 实体集合
     * @return 是否保存成功
     */
    boolean batchSaveEntity(Collection<T> ts);

    /**
     * 根据key删除实体
     *
     * @param key key
     * @return 是否删除成功
     */
    boolean deleteByKey(K key);

    /**
     * 根据key集合批量删除实体
     *
     * @param keys key集合
     * @return 是否删除成功
     */
    boolean deleteByKeys(Collection<K> keys);

    /**
     * 根据某字段删除数据
     * 字段值为null时不进行删除
     *
     * @param field      指定字段
     * @param fieldValue 指定字段值
     * @return 是否删除成功
     * @since 2.1.2
     */
    <F> boolean deleteByField(SFunction<T, F> field, F fieldValue);

    /**
     * 根据某个字段列表删除数据
     *
     * @param field       字段
     * @param fieldValues 字段值列表删
     * @param <F>         字段值类型
     * @return 是否删除成功
     * @since 2.1.6
     */
    <F> boolean deleteInField(SFunction<T, F> field, Collection<F> fieldValues);

    /**
     * 空页
     *
     * @param <E> 页值类型
     * @return 空页
     * @since 2.1.6
     */
    default <E> Page<E> emptyPage() {
        return Page.of(1, 10);
    }

    /**
     * 根据某个字段范围查询
     * 起始值和结束值有一项边界为空时，该边界按无穷进行处理
     *
     * @param field      字段
     * @param startValue 字段起始值
     * @param endValue   字段结束值
     * @param include    包含边界值标识
     * @param <F>        字段值类型
     * @return 数据列表
     * @since 2.1.6
     */
    <F> List<T> listByFieldRange(SFunction<T, F> field, F startValue, F endValue, Include include);

    /**
     * 根据某个字段范围强制查询
     * 必须包含起始值和结束值时才进行查询，有一项为空，返回空列表
     *
     * @param field      字段
     * @param startValue 字段起始值
     * @param endValue   字段结束值
     * @param include    包含边界值标识
     * @param <F>        字段值类型
     * @return 数据列表
     * @since 2.1.6
     */
    <F> List<T> listByFieldRangeForce(SFunction<T, F> field, F startValue, F endValue, Include include);

    /**
     * 批量保存实体
     * 注意: 该方法不会返回主键id
     * 数据表含特殊字段类型（例如 json 类型）时，拼接sql会报错，
     * 报错时请使用方法 {@link BaseService#saveBatchEntity(Collection)} 替代
     *
     * @param ts 待保存的数据
     * @since 2.2.0
     */
    boolean saveBatchList(Collection<T> ts);

    /**
     * 批量保存实体
     * 数据量超过参数batchSize时，会分批保存
     * 注意: 该方法不会返回主键id
     * 数据表含特殊字段类型（例如 json 类型）时，拼接sql会报错，
     * 报错时请使用方法 {@link BaseService#saveBatchEntity(Collection)} 替代
     *
     * @param ts        待保存的数据
     * @param batchSize 每一批保存的数据量
     * @since 2.2.0
     */
    boolean saveBatchList(Collection<T> ts, int batchSize);

    /**
     * 根据主键批量修改实体
     *
     * @param ts 待保存的数据
     * @since 2.2.0
     */
    boolean updateBatchList(Collection<T> ts);

    /**
     * 根据主键批量修改实体，暂时只支持MySQL
     * 数据量超过参数batchSize时，会分批修改
     *
     * @param ts        待修改的数据
     * @param batchSize 每一批修改的数据量
     * @since 2.2.0
     */
    boolean updateBatchList(Collection<T> ts, int batchSize);

    /**
     * 条件去重查询某个字段
     *
     * @param field        字段
     * @param queryWrapper 查询条件
     * @param <F>          字段类型
     * @return 字段去重集合
     * @since 2.4.0
     */
    <F> List<F> listDistinctField(LambdaQueryWrapper<T> queryWrapper, SFunction<T, F> field);

    /**
     * 判断某个字段的值是否存在
     *
     * @param field 字段
     * @param value 字段值
     * @param <F>   字段类型
     * @return 是否存在
     * @since 2.5.0
     */
    <F> boolean existsField(SFunction<T, F> field, F value);

    /**
     * 判断除指定的key之外,某个字段的值是否存在
     *
     * @param excludeKey 排除的key
     * @param field      字段
     * @param value      字段值
     * @param <F>        字段类型
     * @return 是否存在
     * @since 2.5.0
     */
    <F> boolean existsFieldExclude(K excludeKey, SFunction<T, F> field, F value);

    /**
     * 根据字段查询
     *
     * @param condition 字段条件
     * @return 数据列表
     * @since v2.5.0
     */
    List<T> listByCondition(Condition<T, ?> condition);

    /**
     * 根据两个字段查询
     *
     * @param condition1 字段条件1
     * @param condition2 字段条件2
     * @return 数据列表
     * @since v2.5.0
     */
    List<T> listByCondition(Condition<T, ?> condition1, Condition<T, ?> condition2);

    /**
     * 根据三个字段查询
     *
     * @param condition1 字段条件1
     * @param condition2 字段条件2
     * @param condition3 字段条件3
     * @return 数据列表
     * @since v2.5.0
     */
    List<T> listByCondition(Condition<T, ?> condition1, Condition<T, ?> condition2, Condition<T, ?> condition3);
}
