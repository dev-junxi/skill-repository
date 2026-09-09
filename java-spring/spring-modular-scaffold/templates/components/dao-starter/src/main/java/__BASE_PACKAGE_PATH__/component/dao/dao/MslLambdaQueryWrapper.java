package __BASE_PACKAGE__.component.dao.dao;

import static com.baomidou.mybatisplus.core.enums.WrapperKeyword.APPLY;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import __BASE_PACKAGE__.component.dao.match.Match;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wang.linqiao
 * date 2022-11-21
 * time 20:46
 */
public class MslLambdaQueryWrapper<T> extends LambdaQueryWrapper<T> {
    public MslLambdaQueryWrapper() {}

    MslLambdaQueryWrapper(
            T entity,
            Class<T> entityClass,
            SharedString sqlSelect,
            AtomicInteger paramNameSeq,
            Map<String, Object> paramNameValuePairs,
            MergeSegments mergeSegments,
            SharedString paramAlias,
            SharedString lastSql,
            SharedString sqlComment,
            SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        // sqlSelect不处理
        this.expression = mergeSegments;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    public MslLambdaQueryWrapper(T entity) {
        super(entity);
    }

    public MslLambdaQueryWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    public MslLambdaQueryWrapper<T> eqIfExists(SFunction<T, ?> column, Object val) {
        super.eq(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> neIfExists(SFunction<T, ?> column, Object val) {
        super.ne(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> gtIfExists(SFunction<T, ?> column, Object val) {
        super.gt(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> geIfExists(SFunction<T, ?> column, Object val) {
        super.ge(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> ltIfExists(SFunction<T, ?> column, Object val) {
        super.lt(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> leIfExists(SFunction<T, ?> column, Object val) {
        super.le(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> likeIfExists(SFunction<T, ?> column, Object val) {
        super.like(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> notLikeIfExists(SFunction<T, ?> column, Object val) {
        super.notLike(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> likeLeftIfExists(SFunction<T, ?> column, Object val) {
        super.likeLeft(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> likeRightIfExists(SFunction<T, ?> column, Object val) {
        super.likeRight(val != null, column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> eqIfNotBlank(SFunction<T, ?> column, String val) {
        super.eq(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> neIfNotBlank(SFunction<T, ?> column, String val) {
        super.ne(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> gtIfNotBlank(SFunction<T, ?> column, String val) {
        super.gt(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> geIfNotBlank(SFunction<T, ?> column, String val) {
        super.ge(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> ltIfNotBlank(SFunction<T, ?> column, String val) {
        super.lt(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> leIfNotBlank(SFunction<T, ?> column, String val) {
        super.le(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> likeIfNotBlank(SFunction<T, ?> column, String val) {
        super.like(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> notLikeIfNotBlank(SFunction<T, ?> column, String val) {
        super.notLike(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> likeLeftIfNotBlank(SFunction<T, ?> column, String val) {
        super.likeLeft(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> likeRightIfNotBlank(SFunction<T, ?> column, String val) {
        super.likeRight(StringUtils.isNoneBlank(val), column, val);
        return this;
    }

    public MslLambdaQueryWrapper<T> match(SFunction<T, ?> column, Match match) {
        if (match != null) {
            match.doMatch(this, column);
        }
        return this;
    }

    public MslLambdaQueryWrapper<T> andThis(boolean condition, Consumer<MslLambdaQueryWrapper<T>> consumer) {
        return andThis(condition).addNestedConditionThis(condition, consumer);
    }

    protected MslLambdaQueryWrapper<T> andThis(boolean condition) {
        super.and(condition);
        return this;
    }

    public MslLambdaQueryWrapper<T> andThis(Consumer<MslLambdaQueryWrapper<T>> consumer) {
        return andThis(true, consumer);
    }

    public MslLambdaQueryWrapper<T> orThis(boolean condition, Consumer<MslLambdaQueryWrapper<T>> consumer) {
        return orThis(condition).addNestedConditionThis(condition, consumer);
    }

    protected MslLambdaQueryWrapper<T> orThis(boolean condition) {
        super.or(condition);
        return this;
    }

    public MslLambdaQueryWrapper<T> orThis(Consumer<MslLambdaQueryWrapper<T>> consumer) {
        return orThis(true, consumer);
    }

    @Override
    public MslLambdaQueryWrapper<T> or() {
        super.or();
        return this;
    }

    public LambdaQueryWrapper<T> toLambdaQueryWrapper() {
        return this;
    }

    /**
     * 多重嵌套查询条件
     *
     * @param condition 查询条件值
     */
    protected MslLambdaQueryWrapper<T> addNestedConditionThis(
            boolean condition, Consumer<MslLambdaQueryWrapper<T>> consumer) {
        maybeDo(condition, () -> {
            final MslLambdaQueryWrapper<T> instance = instance();
            consumer.accept(instance);
            appendSqlSegments(APPLY, instance);
        });
        return this;
    }

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected MslLambdaQueryWrapper<T> instance() {
        return new MslLambdaQueryWrapper<>(
                getEntity(),
                getEntityClass(),
                null,
                paramNameSeq,
                paramNameValuePairs,
                new MergeSegments(),
                paramAlias,
                SharedString.emptyString(),
                SharedString.emptyString(),
                SharedString.emptyString());
    }
}
