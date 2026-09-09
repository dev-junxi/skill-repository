package __BASE_PACKAGE__.component.dao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import __BASE_PACKAGE__.component.dao.condition.Condition;
import com.msl.model.utils.HasKey;
import com.msl.view.Include;
import com.msl.view.View;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author wanglq
 * Date 2022/11/15
 * Time 14:32
 */
public interface BaseService<V extends View<T>, T extends HasKey<K>, K> {

    /**
     * 构造单实体view
     *
     * @since 2.1.3
     */
    V buildView(T entity);

    /**
     * 构造实体列表view
     *
     * @since 2.1.3
     */
    List<V> buildView(Collection<T> entity);

    /**
     * 构造分页view
     *
     * @since 2.1.3
     */
    IPage<V> buildView(IPage<T> entity);

    /**
     * 根据key集合查询ModelView
     *
     * @param keys key集合
     * @return view集合
     */
    List<V> listViewByKeys(Collection<K> keys);

    /**
     * 根据key集合查询实体
     *
     * @param keys key集合
     * @return view集合
     * @since 2.1.3
     */
    List<T> listEntityByKeys(Collection<K> keys);

    /**
     * 查询所有ModelView
     */
    List<V> listAllView();

    /**
     * 根据key查询ModelView
     *
     * @param key key
     * @return view
     */
    Optional<V> getViewByKey(K key);

    /**
     * 根据key查询实体
     *
     * @param key key
     * @return 实体
     */
    Optional<T> getEntityByKey(K key);

    /**
     * 根据key删除数据
     *
     * @param key key
     * @return 是否删除成功
     */
    boolean deleteByKey(K key);

    /**
     * 根据key集合批量删除数据
     *
     * @param keys keys
     * @return 是否删除成功任一数据
     */
    boolean deleteByKeys(Collection<K> keys);

    /**
     * 新增实体
     *
     * @param entity 实体
     * @return 是否新增成功
     */
    boolean saveEntity(T entity);

    /**
     * 根据 key 修改实体
     *
     * @param entity 实体
     * @return 是否修改成功
     */
    boolean updateByKey(T entity);

    /**
     * 批量保存实体
     * 此方法直接调用 Mybatis-Plus 自带的批量保存，实际进行的循环操作
     * 替代方法为{@link BaseService#saveBatchList(Collection)}
     *
     * @param entities 批量实体
     * @return 是否新增成功任一数据
     */
    boolean saveBatchEntity(Collection<T> entities);

    /**
     * 根据某个字段查询列表数据
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @param <F>        字段值类型
     * @since 2.1.3
     */
    <F> List<V> listByField(SFunction<T, F> field, F fieldValue);

    /**
     * 根据某个字段查询单条数据
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @param <F>        字段值类型
     * @since 2.1.3
     */
    <F> Optional<V> getOneByField(SFunction<T, F> field, F fieldValue);

    /**
     * 根据某个字段查询单条数据(拼接 limit 1)
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @param <F>        字段值类型
     * @since 2.1.3
     */
    <F> Optional<V> getLimitOneByField(SFunction<T, F> field, F fieldValue);

    /**
     * 根据某个字段集合查询列表数据
     *
     * @param field       字段
     * @param fieldValues 字段值集合
     * @param <F>         字段值类型
     * @since 2.1.3
     */
    <F> List<V> listInField(SFunction<T, F> field, Collection<F> fieldValues);

    /**
     * 根据某个字段删除数据
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @param <F>        字段值类型
     * @return 是否删除成功
     * @since 2.1.6
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
    <F> List<V> listByFieldRange(SFunction<T, F> field, F startValue, F endValue, Include include);

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
    <F> List<V> listByFieldRangeForce(SFunction<T, F> field, F startValue, F endValue, Include include);

    /**
     * 批量保存实体
     * 注意: 该方法不会返回主键id，
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
     * 根据主键批量修改实体，暂时只支持MySQL
     * 注意：使用此方法时，需要在数据库中链接地址中添加参数 allowMultiQueries=true,
     * 如果项目中使用 druid 连接池，并且配置 wall 过滤器，需要在 wall 配置中添加 multi-statement-allow: true
     * 注意：在2.3.0版本底层改为mybatis-plus的实现，故可忽略上面的注意事项
     *
     * @param ts 待修改的数据
     * @since 2.2.0
     */
    boolean updateBatchList(Collection<T> ts);

    /**
     * 根据主键批量修改实体，暂时只支持MySQL
     * 数据量超过参数batchSize时，会分批修改
     * 注意：使用此方法时，需要在数据库中链接地址中添加参数 allowMultiQueries=true,
     * 如果项目中使用 druid 连接池，并且配置 wall 过滤器，需要在 wall 配置中添加 multi-statement-allow: true
     * 注意：在2.3.0版本底层改为mybatis-plus的实现，故可忽略上面的注意事项
     *
     * @param ts        待修改的数据
     * @param batchSize 每一批修改的数据量
     * @since 2.2.0
     */
    boolean updateBatchList(Collection<T> ts, int batchSize);

    /**
     * 根据字段查询
     *
     * @param condition 字段条件
     * @return 数据列表
     * @since v2.5.0
     */
    List<V> listByCondition(Condition<T, ?> condition);

    /**
     * 根据两个字段查询
     *
     * @param condition1 字段条件1
     * @param condition2 字段条件2
     * @return 数据列表
     * @since v2.5.0
     */
    List<V> listByCondition(Condition<T, ?> condition1, Condition<T, ?> condition2);

    /**
     * 根据三个字段查询
     *
     * @param condition1 字段条件1
     * @param condition2 字段条件2
     * @param condition3 字段条件3
     * @return 数据列表
     * @since v2.5.0
     */
    List<V> listByCondition(Condition<T, ?> condition1, Condition<T, ?> condition2, Condition<T, ?> condition3);
}
