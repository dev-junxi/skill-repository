package __BASE_PACKAGE__.component.dao.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import __BASE_PACKAGE__.component.dao.condition.Condition;
import __BASE_PACKAGE__.component.dao.dao.BaseDao;
import __BASE_PACKAGE__.component.dao.service.BaseService;
import __BASE_PACKAGE__.component.dao.utils.BuilderUtils;
import com.msl.model.builder.ModelBuilder;
import com.msl.model.utils.HasKey;
import com.msl.view.Include;
import com.msl.view.View;
import com.msl.view.mapper.ViewMapper;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wanglq
 * Date 2022/11/15
 * Time 14:32
 */
@SuppressWarnings("all")
public class BaseServiceImpl<D extends BaseDao<?, T, K>, V extends View<T>, T extends HasKey<K>, K extends Serializable>
        implements BaseService<V, T, K> {
    @Autowired
    protected ModelBuilder modelBuilder;

    @Autowired
    protected D baseDao;

    @Autowired
    protected ViewMapper viewMapper;

    /**
     * 根据key集合查询ModelView
     *
     * @param keys key集合
     * @return view集合
     */
    @Override
    public List<V> listViewByKeys(Collection<K> keys) {
        return buildView(baseDao.listByKeys(keys));
    }

    /**
     * 根据key集合查询实体
     *
     * @param keys key集合
     * @return view集合
     * @since 2.1.3
     */
    @Override
    public List<T> listEntityByKeys(Collection<K> keys) {
        return baseDao.listByKeys(keys);
    }

    /**
     * 根据key查询ModelView
     *
     * @param key key
     * @return view
     */
    @Override
    public Optional<V> getViewByKey(K key) {
        return baseDao.getEntityByKey(key).map(this::buildView);
    }

    /**
     * 根据key查询实体
     *
     * @param key key
     * @return 实体
     */
    @Override
    public Optional<T> getEntityByKey(K key) {
        return baseDao.getEntityByKey(key);
    }

    /**
     * 根据key删除数据
     *
     * @param key key
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByKey(K key) {
        return baseDao.deleteByKey(key);
    }

    /**
     * 根据key集合批量删除数据
     *
     * @param keys keys
     * @return 是否删除成功任一数据
     */
    @Override
    public boolean deleteByKeys(Collection<K> keys) {
        return baseDao.deleteByKeys(keys);
    }

    @Override
    public List<V> listAllView() {
        return buildView(baseDao.listAll());
    }

    @Override
    public boolean saveEntity(T entity) {
        return baseDao.saveEntity(entity);
    }

    /**
     * 根据 key 修改实体
     *
     * @param entity 实体
     * @return 是否修改成功
     */
    @Override
    public boolean updateByKey(T entity) {
        return baseDao.updateEntityByKey(entity);
    }

    /**
     * 批量保存实体
     * 此方法直接调用 Mybatis-Plus 自带的批量保存，实际进行的循环操作
     * 另实现有无循环方法为{@link BaseService#saveBatchList(Collection)}
     *
     * @param entities 批量实体
     * @return 是否新增成功任一数据
     */
    @Override
    public boolean saveBatchEntity(Collection<T> entities) {
        return baseDao.batchSaveEntity(entities);
    }

    /**
     * 根据某个字段查询列表数据
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @since 2.1.3
     */
    @Override
    public <F> List<V> listByField(SFunction<T, F> field, F fieldValue) {
        return buildView(baseDao.listByField(field, fieldValue));
    }

    /**
     * 根据某个字段查询单条数据
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @since 2.1.3
     */
    @Override
    public <F> Optional<V> getOneByField(SFunction<T, F> field, F fieldValue) {
        return baseDao.getOneByField(field, fieldValue).map(this::buildView);
    }

    /**
     * 根据某个字段查询单条数据(拼接 limit 1)
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @since 2.1.3
     */
    @Override
    public <F> Optional<V> getLimitOneByField(SFunction<T, F> field, F fieldValue) {
        return baseDao.getLimitOneByField(field, fieldValue).map(this::buildView);
    }

    /**
     * 根据某个字段集合查询列表数据
     *
     * @param field       字段
     * @param fieldValues 字段值集合
     * @since 2.1.3
     */
    @Override
    public <F> List<V> listInField(SFunction<T, F> field, Collection<F> fieldValues) {
        return buildView(baseDao.listInField(field, fieldValues));
    }

    /**
     * 根据某个字段删除数据
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @return 是否删除成功
     * @since 2.1.6
     */
    @Override
    public <F> boolean deleteByField(SFunction<T, F> field, F fieldValue) {
        return baseDao.deleteByField(field, fieldValue);
    }

    /**
     * 根据某个字段列表删除数据
     *
     * @param field       字段
     * @param fieldValues 字段值列表删
     * @return 是否删除成功
     * @since 2.1.6
     */
    @Override
    public <F> boolean deleteInField(SFunction<T, F> field, Collection<F> fieldValues) {
        return baseDao.deleteInField(field, fieldValues);
    }

    /**
     * 根据某个字段范围查询
     * 起始值和结束值有一项边界为空时，该边界按无穷进行处理
     *
     * @param field      字段
     * @param startValue 字段起始值
     * @param endValue   字段结束值
     * @param include    包含边界值标识
     * @return 数据列表
     * @since 2.1.6
     */
    @Override
    public <F> List<V> listByFieldRange(SFunction<T, F> field, F startValue, F endValue, Include include) {
        return buildView(baseDao.listByFieldRange(field, startValue, endValue, include));
    }

    /**
     * 根据某个字段范围强制查询
     * 必须包含起始值和结束值时才进行查询，有一项为空，返回空列表
     *
     * @param field      字段
     * @param startValue 字段起始值
     * @param endValue   字段结束值
     * @param include    包含边界值标识
     * @return 数据列表
     * @since 2.1.6
     */
    @Override
    public <F> List<V> listByFieldRangeForce(SFunction<T, F> field, F startValue, F endValue, Include include) {
        return buildView(baseDao.listByFieldRangeForce(field, startValue, endValue, include));
    }

    /**
     * 批量保存实体
     * 注意: 该方法不会返回主键id
     * 数据表含特殊字段类型（例如 json 类型）时，拼接sql会报错，
     * 报错时请使用方法 {@link BaseService#saveBatchEntity(Collection)} 替代
     *
     * @param ts 待保存的数据
     * @since 2.2.0
     */
    @Override
    public boolean saveBatchList(Collection<T> ts) {
        return baseDao.saveBatchList(ts);
    }

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
    @Override
    public boolean saveBatchList(Collection<T> ts, int batchSize) {
        return baseDao.saveBatchList(ts, batchSize);
    }

    /**
     * 根据主键批量修改实体
     *
     * @param ts 待修改的数据
     * @since 2.2.0
     */
    @Override
    public boolean updateBatchList(Collection<T> ts) {
        return baseDao.updateBatchList(ts);
    }

    /**
     * 根据主键批量修改实体，暂时只支持MySQL
     * 数据量超过参数batchSize时，会分批修改
     *
     * @param ts        待修改的数据
     * @param batchSize 每一批修改的数据量
     * @since 2.2.0
     */
    @Override
    public boolean updateBatchList(Collection<T> ts, int batchSize) {
        return baseDao.updateBatchList(ts, batchSize);
    }

    /**
     * 根据字段查询
     *
     * @param condition 字段条件
     * @return 数据列表
     * @since v2.5.0
     */
    @Override
    public List<V> listByCondition(Condition<T, ?> condition) {
        return buildView(baseDao.listByCondition(condition));
    }

    /**
     * 根据两个字段查询
     *
     * @param condition1 字段条件1
     * @param condition2 字段条件2
     * @return 数据列表
     * @since v2.5.0
     */
    @Override
    public List<V> listByCondition(Condition<T, ?> condition1, Condition<T, ?> condition2) {
        return buildView(baseDao.listByCondition(condition1, condition2));
    }

    /**
     * 根据三个字段查询
     *
     * @param condition1 字段条件1
     * @param condition2 字段条件2
     * @param condition3 字段条件3
     * @return 数据列表
     * @since v2.5.0
     */
    @Override
    public List<V> listByCondition(Condition<T, ?> condition1, Condition<T, ?> condition2, Condition<T, ?> condition3) {
        return buildView(baseDao.listByCondition(condition1, condition2, condition3));
    }

    /**
     * 构造实体列表view
     */
    public List<V> buildView(Collection<T> list) {
        return BuilderUtils.buildView(list, modelBuilder, viewMapper);
    }

    public Page<V> buildView(Page<T> page) {
        return BuilderUtils.buildView(page, modelBuilder, viewMapper);
    }

    public IPage<V> buildView(IPage<T> page) {
        return BuilderUtils.buildView(page, modelBuilder, viewMapper);
    }

    @Override
    public V buildView(T entity) {
        return BuilderUtils.buildView(entity, modelBuilder, viewMapper);
    }
}
