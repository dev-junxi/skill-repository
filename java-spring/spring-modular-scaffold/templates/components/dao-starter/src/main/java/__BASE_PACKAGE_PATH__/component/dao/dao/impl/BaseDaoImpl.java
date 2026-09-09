package __BASE_PACKAGE__.component.dao.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import __BASE_PACKAGE__.component.dao.condition.Condition;
import __BASE_PACKAGE__.component.dao.dao.BaseDao;
import __BASE_PACKAGE__.component.dao.dao.DaoWrapper;
import __BASE_PACKAGE__.component.dao.dao.MslLambdaQueryWrapper;
import __BASE_PACKAGE__.component.dao.mapper.DefaultBatchMapper;
import __BASE_PACKAGE__.component.dao.utils.DaoCollections;
import __BASE_PACKAGE__.component.dao.utils.Optionals;
import __BASE_PACKAGE__.component.dao.utils.SqlUtils;
import com.msl.model.builder.ModelBuilder;
import com.msl.model.builder.annotation.OneMatch;
import com.msl.model.utils.HasKey;
import com.msl.view.Include;
import com.msl.view.KeyColumn;
import com.msl.view.annotation.ManyToOne;
import com.msl.view.annotation.OneToOne;
import com.msl.view.namespace.OneToManyNameSpace;
import com.msl.view.namespace.OneToOneNameSpace;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wanglq
 * Date 2022/9/20
 * Time 下午3:20
 */
@SuppressWarnings("all")
public abstract class BaseDaoImpl<M extends BaseMapper<T>, T extends HasKey<K>, K extends Serializable>
        extends ServiceImpl<M, T> implements BaseDao<M, T, K>, DaoWrapper<T>, InitializingBean {
    @Autowired(required = false)
    protected ModelBuilder modelBuilder;

    @Autowired
    private DefaultBatchMapper batchMapper;

    private Class<T> clazz;
    private String keyName;
    private static final int DEFAULT_UPDATE_BATCH_SIZE = 200;
    private static final int DEFAULT_IN_BATCH_SIZE = 1000;

    /**
     * 根据key集合查询数据
     *
     * @param keys key集合
     * @return map数据集合，key为key,value为实体
     */
    @Override
    public Map<K, T> listMapByKeys(Collection<K> keys) {
        return listByKeys(keys).stream().collect(Collectors.toMap(T::gainKey, Function.identity()));
    }

    /**
     * 根据key集合查询数据
     *
     * @param keys key集合
     * @return 数据集合
     */
    @Override
    public List<T> listByKeys(Collection<K> keys) {
        return inSplit(keys, l -> super.list(qrWrapper().in(keyName, l)));
    }

    /**
     * 根据某字段查询列表数据
     *
     * @param field      指定字段
     * @param fieldValue 指定字段值
     * @return map数据集合，key为key,value为实体
     * @since 2.1.0
     */
    @Override
    public <F> List<T> listByField(SFunction<T, F> field, F fieldValue) {
        return Optional.ofNullable(field)
                .filter(f -> fieldValue != null)
                .map(f -> lQrWrapper().eq(field, fieldValue))
                .map(super::list)
                .orElse(new ArrayList<>(0));
    }

    /**
     * 根据某字段值集合批量查询列表数据
     * 字段值集合为空时不进行查询
     *
     * @param field       指定字段
     * @param fieldValues 指定字段值集合
     * @return 实体集合
     * @since 2.1.0
     */
    @Override
    public <F> List<T> listInField(SFunction<T, F> field, Collection<F> fieldValues) {
        if (field == null || CollectionUtils.isEmpty(fieldValues)) {
            return new ArrayList<>();
        }
        return inSplit(fieldValues, l -> super.list(lQrWrapper().in(field, l)));
    }

    /**
     * 根据某字段查询单条数据,存在多条数据时抛出异常
     *
     * @param field      指定字段
     * @param fieldValue 指定字段值
     * @return 实体
     * @since 2.1.0
     */
    @Override
    public <F> Optional<T> getOneByField(SFunction<T, F> field, F fieldValue) {
        return Optional.ofNullable(field)
                .filter(f -> fieldValue != null)
                .map(f -> lQrWrapper().eq(field, fieldValue))
                .map(super::getOne);
    }

    /**
     * 根据某字段查询单条数据，自动拼接 limit 1
     *
     * @param field      指定字段
     * @param fieldValue 指定字段值
     * @return 实体
     * @since 2.1.0
     */
    @Override
    public <F> Optional<T> getLimitOneByField(SFunction<T, F> field, F fieldValue) {
        return Optional.ofNullable(field)
                .filter(f -> fieldValue != null)
                .map(f -> lQrWrapper().eq(field, fieldValue).last("limit 1"))
                .map(super::getOne);
    }

    /**
     * 根据key获取实体数据
     *
     * @param key key
     * @return 实体
     */
    @Override
    public Optional<T> getEntityByKey(K key) {
        return Optional.ofNullable(key).map(k -> qrWrapper().eq(keyName, k)).map(super::getOne);
    }

    /**
     * 根据id更新实体
     *
     * @param t 实体
     * @return 是否保存成功
     */
    @Override
    public boolean updateEntityByKey(T t) {
        // 原版用的是自制 Optional 的 mapWith(supplier, biFunction)，JDK Optional 没有；
        // 语义等价改写：值存在时才构造 wrapper 并执行 update
        return Optional.ofNullable(t)
                .filter(e -> e.gainKey() != null)
                .map(e -> super.update(t, qrWrapper().eq(keyName, e.gainKey())))
                .orElse(false);
    }

    /**
     * 根据key删除实体
     *
     * @param key key
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByKey(K key) {
        return Optional.ofNullable(key)
                .map(k -> qrWrapper().eq(keyName, k))
                .map(super::remove)
                .orElse(false);
    }

    /**
     * 根据id集合批量删除实体
     *
     * @param keys id集合
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByKeys(Collection<K> keys) {
        return Optionals.ofEmpty(keys)
                .map(s -> qrWrapper().in(keyName, keys))
                .map(super::remove)
                .orElse(false);
    }

    /**
     * 根据某字段删除数据
     * 字段值为null时不进行删除
     *
     * @param field      指定字段
     * @param fieldValue 指定字段值
     * @return 是否删除成功
     * @since 2.1.2
     */
    @Override
    public <F> boolean deleteByField(SFunction<T, F> field, F fieldValue) {
        return Optional.ofNullable(fieldValue)
                .map(f -> lQrWrapper().eq(field, fieldValue))
                .map(super::remove)
                .orElse(false);
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
        return Optionals.ofEmpty(fieldValues)
                .map(f -> lQrWrapper().in(field, fieldValues))
                .map(super::remove)
                .orElse(false);
    }

    /**
     * 根据某个字段范围查询
     * 起始值和结束值有一项边界为空时，该边界按无穷进行处理
     *
     * @param field      字段
     * @param startValue 字段起始值
     * @param endValue   字段结束值
     * @param include    包含边界值标识,默认为{@link Include#ALL}
     * @return 数据列表
     * @since 2.1.6
     */
    @Override
    public <F> List<T> listByFieldRange(SFunction<T, F> field, F startValue, F endValue, Include include) {
        if (include == null) {
            include = Include.ALL;
        }
        MslLambdaQueryWrapper<T> wrapper = lQrWrapper();
        if (include.hasLeft()) {
            wrapper.geIfExists(field, startValue);
        } else {
            wrapper.gtIfExists(field, startValue);
        }
        if (include.hasRight()) {
            wrapper.leIfExists(field, endValue);
        } else {
            wrapper.ltIfExists(field, endValue);
        }
        return super.list(wrapper);
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
    public <F> List<T> listByFieldRangeForce(SFunction<T, F> field, F startValue, F endValue, Include include) {
        if (startValue == null || endValue == null) {
            return Lists.newArrayList();
        }
        if (include == null) {
            include = Include.ALL;
        }
        MslLambdaQueryWrapper<T> wrapper = lQrWrapper();
        if (include.hasLeft()) {
            wrapper.ge(field, startValue);
        } else {
            wrapper.gt(field, startValue);
        }
        if (include.hasRight()) {
            wrapper.le(field, endValue);
        } else {
            wrapper.lt(field, endValue);
        }
        return super.list(wrapper);
    }

    @Override
    public List<T> listAll() {
        return super.list();
    }

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
     * <p>This method allows the bean instance to perform validation of its overall
     * configuration and final initialization when all bean properties have been set.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an
     *                   essential property) or if initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        modelBuilder.lazyBuild(clazz, this::listMapByKeys, clazz).valueFromSelf(clazz, T::gainKey);
        for (Field field : ReflectionUtils.getAllFields(clazz)) {
            field.setAccessible(true);
            buildOneMatch(field);
            buildManyToOne(field);
            buildOneToOne(field);
        }
        addModelBuilder(modelBuilder);
        Method method = clazz.getDeclaredMethod("gainKey");
        method.setAccessible(true);
        KeyColumn column = method.getAnnotation(KeyColumn.class);
        if (column == null) {
            throw new IllegalStateException(
                    String.format("实体类%s的gainKey方法必须通过注解@KeyColumn指定对应的数据库字段名", clazz.getSimpleName()));
        }
        keyName = column.value();
    }

    private void buildManyToOne(Field field) throws Exception {
        ManyToOne oneMatch = field.getAnnotation(ManyToOne.class);
        if (oneMatch == null) {
            return;
        }

        String columnKey = LambdaUtils.formatKey(field.getName());
        ColumnCache columnCache = LambdaUtils.getColumnMap(clazz).get(columnKey);
        if (columnCache == null) {
            return;
        }
        Function<T, Object> fieldGetter = createFieldGetter(field);
        //        MethodHandle getter = createGetterMethodHandle(field);
        for (Class<?> oneClass : oneMatch.value()) {
            if (oneClass == null) {
                continue;
            }
            modelBuilder.extractId(clazz, fieldGetter, oneClass);
            modelBuilder.lazyBuild(
                    oneClass,
                    oneIds -> {
                        List<T> list = inSplit(oneIds, l -> list(qrWrapper().in(columnCache.getColumn(), l)));
                        return list.stream().collect(Collectors.groupingBy(fieldGetter));
                    },
                    OneToManyNameSpace.put(oneClass, clazz));
        }
    }

    /**
     * in 值查询分批
     *
     * @param inValueList 原始in值列表
     * @param function    查询方法
     * @param <E>
     * @param <V>
     * @return
     */
    private <E, V> List<V> inSplit(Collection<E> inValueList, Function<? super Collection<E>, List<V>> function) {
        if (CollectionUtils.isEmpty(inValueList)) {
            return new ArrayList<>();
        }
        // 不超过限定的 in 值数量时,直接查询
        if (inValueList.size() <= DEFAULT_IN_BATCH_SIZE) {
            return function.apply(inValueList);
        }
        // 超过限定 in 值数量时,分批查询
        List<V> ans = new ArrayList<>();
        for (List<E> sl : DaoCollections.split(inValueList, DEFAULT_IN_BATCH_SIZE)) {
            ans.addAll(function.apply(sl));
        }
        return ans;
    }

    /**
     * 辅助方法：创建字段访问Function
     */
    private Function<T, Object> createFieldGetter(Field field) throws Exception {
        MethodHandle getterMethodHandle = createGetterMethodHandle(field);
        return o -> invokeMethodHandle(getterMethodHandle, o);
    }

    /**
     * 辅助方法：创建字段访问方法句柄
     */
    private MethodHandle createGetterMethodHandle(Field field) throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
        return lookup.unreflectGetter(field);
    }

    /**
     * 辅助方法：安全调用方法句柄
     */
    private Object invokeMethodHandle(MethodHandle handle, Object target) {
        try {
            return handle.invoke(target);
        } catch (Throwable e) {
            throw new RuntimeException("获取实体字段值异常", e);
        }
    }

    private void buildOneToOne(Field field) throws Exception {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        if (oneToOne != null) {
            Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(clazz);
            String column =
                    columnMap.get(LambdaUtils.formatKey(field.getName())).getColumn();
            Function<T, Object> fieldGetter = createFieldGetter(field);
            for (Class<?> oneClass : oneToOne.value()) {
                if (oneClass == null) {
                    continue;
                }
                modelBuilder.extractId(clazz, fieldGetter, oneClass);
                modelBuilder.lazyBuild(
                        oneClass,
                        oneIds -> {
                            List<T> list = inSplit(oneIds, l -> list(qrWrapper().in(column, l)));
                            return list.stream().collect(Collectors.toMap(fieldGetter, Function.identity()));
                        },
                        OneToOneNameSpace.put(oneClass, clazz));
            }
        }
    }

    private void buildOneMatch(Field field) throws Exception {
        OneMatch oneMatch = field.getAnnotation(OneMatch.class);
        if (oneMatch != null) {
            for (Class<?> idNameSpace : oneMatch.value()) {
                if (idNameSpace == null) {
                    continue;
                }
                Function<T, Object> fieldGetter = createFieldGetter(field);
                modelBuilder.extractId(clazz, fieldGetter, idNameSpace);
            }
        }
    }

    public void addModelBuilder(ModelBuilder modelBuilder) {}

    /**
     * 保存实体
     *
     * @param t 实体
     */
    @Override
    public boolean saveEntity(T t) {
        return Optional.ofNullable(t).map(super::save).orElse(false);
    }

    /**
     * 批量保存实体
     * 此方法直接调用 Mybatis-Plus 自带的批量保存，实际进行的循环操作
     * 另实现有无循环方法为{@link BaseBatchDao#saveBatchList(Collection)}
     *
     * @param ts 实体集合
     * @return 是否保存成功
     */
    @Override
    public boolean batchSaveEntity(Collection<T> ts) {
        return Optionals.ofEmpty(ts).map(super::saveBatch).orElse(false);
    }

    /**
     * 批量保存实体
     * 注意: 该方法不会返回主键id
     *
     * @param ts 待保存的数据
     * @since 2.2.0
     */
    @Override
    public boolean saveBatchList(Collection<T> ts) {
        return saveBatchList(ts, DEFAULT_UPDATE_BATCH_SIZE);
    }

    /**
     * 批量保存实体
     * 数据量超过参数batchSize时，会分批保存
     * 注意: 该方法不会返回主键id
     *
     * @param ts        待保存的数据
     * @param batchSize 每一批保存的数据量
     * @since 2.2.0
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveBatchList(Collection<T> ts, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        if (CollectionUtils.isEmpty(ts)) {
            return false;
        }
        // 数据量小于等于batchSize时，直接保存
        if (ts.size() <= batchSize) {
            return batchSaveEntity(ts);
        }
        // 数据量大于batchSize时，分批保存
        return DaoCollections.split(ts, batchSize).stream()
                .map(this::batchSaveEntity)
                .reduce((a, b) -> a | b)
                .orElse(false);
    }

    /**
     * 根据主键批量修改实体
     *
     * @param ts 待保存的数据
     * @since 2.2.0
     */
    @Override
    public boolean updateBatchList(Collection<T> ts) {
        return updateBatchList(ts, DEFAULT_UPDATE_BATCH_SIZE);
    }

    /**
     * 根据主键批量修改实体，暂时只支持MySQL
     * 数据量超过参数batchSize时，会分批修改
     *
     * @param ts        待修改的数据
     * @param batchSize 每一批修改的数据量
     * @since 2.2.0
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateBatchList(Collection<T> ts, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        if (CollectionUtils.isEmpty(ts)) {
            return false;
        }
        // 数据量小于等于batchSize时，直接修改
        if (ts.size() <= batchSize) {
            return updateBatchById(ts);
        }
        // 数据量大于batchSize时，分批修改
        return DaoCollections.split(ts, batchSize).stream()
                .map(this::updateBatchById)
                .reduce((a, b) -> a | b)
                .orElse(false);
    }

    /**
     * 条件去重查询某个字段
     *
     * @param queryWrapper 查询条件
     * @param field        字段
     * @return 字段去重集合
     * @since 2.4.0
     */
    @Override
    public <F> List<F> listDistinctField(LambdaQueryWrapper<T> queryWrapper, SFunction<T, F> field) {
        String column = SqlUtils.getColumn(field);
        SqlUtils.setSelect(queryWrapper, "DISTINCT " + column);
        List<T> ts = baseMapper.selectList(queryWrapper);
        return DaoCollections.toList(ts, t -> field.apply(t));
    }

    /**
     * 判断某个字段的值是否存在
     *
     * @param field 字段
     * @param value 字段值
     * @return 是否存在
     * @since 2.5.0
     */
    @Override
    public <F> boolean existsField(SFunction<T, F> field, F value) {
        assert field != null;
        return count(lQrWrapper().eq(field, value)) > 0;
    }

    /**
     * 判断除指定的key之外,某个字段的值是否存在
     *
     * @param excludeKey 排除的key
     * @param field      字段
     * @param value      字段值
     * @return 是否存在
     * @since 2.5.0
     */
    @Override
    public <F> boolean existsFieldExclude(K excludeKey, SFunction<T, F> field, F value) {
        assert excludeKey != null;
        assert field != null;
        return list(lQrWrapper().eq(field, value)).stream().anyMatch(t -> !Objects.equals(t.gainKey(), excludeKey));
    }

    /**
     * 根据字段查询
     *
     * @param condition 字段条件
     * @return 数据列表
     * @since v2.5.0
     */
    @Override
    public List<T> listByCondition(Condition<T, ?> condition) {
        return list(condition.assemble(lQrWrapper()));
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
    public List<T> listByCondition(Condition<T, ?> condition1, Condition<T, ?> condition2) {
        return list(condition2.assemble(condition1.assemble(lQrWrapper())));
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
    public List<T> listByCondition(Condition<T, ?> condition1, Condition<T, ?> condition2, Condition<T, ?> condition3) {
        return list(condition3.assemble(condition2.assemble(condition1.assemble(lQrWrapper()))));
    }
}
