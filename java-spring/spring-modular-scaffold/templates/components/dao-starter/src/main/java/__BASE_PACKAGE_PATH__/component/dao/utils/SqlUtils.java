package __BASE_PACKAGE__.component.dao.utils;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.msl.model.utils.HasKey;
import java.lang.reflect.Field;
import java.util.*;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.checkerframework.checker.units.qual.K;

/**
 * @author wanglq
 * Date 2024/4/16
 * Time 17:52
 */
public class SqlUtils {

    private static final Field selectField;

    static {
        try {
            selectField = LambdaQueryWrapper.class.getDeclaredField("sqlSelect");
            selectField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String saveBatchList(Collection<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        TableInfo table = SqlHelper.table(list.iterator().next().getClass());
        Set<TableFieldInfo> columns = getColumns(list, table);

        if (!columns.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            String insert = String.format(
                    "INSERT INTO `%s` (`%s`) VALUES ",
                    table.getTableName().replace("`", ""),
                    DaoCollections.join(columns, t -> t.getColumn().replace("`", ""), "`,`")
                            .get());
            sb.append(insert);
            for (int i = 0; i < list.size(); i++) {
                String valueFormat = "#{list[" + i + "].%s}";
                String vs = DaoCollections.join(
                                columns, column -> String.format(valueFormat, column.getProperty()), ",")
                        .get();
                sb.append("\n(").append(vs).append("),");
            }
            return sb.substring(0, sb.length() - 1);
        }
        return null;
    }

    public static <T> String updateBatchList(Collection<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        TableInfo table = SqlHelper.table(list.iterator().next().getClass());
        StringBuilder sb = new StringBuilder();
        sb.append("<script>\n\t<foreach collection='list' item='item' separator=';'>")
                .append("\n\t\tUPDATE ")
                .append(table.getTableName())
                .append("\n\t\t<set>");
        List<TableFieldInfo> fieldList = table.getFieldList();
        for (TableFieldInfo fieldInfo : fieldList) {
            sb.append("\n\t\t\t<if test='item.")
                    .append(fieldInfo.getProperty())
                    .append(" != null'>`")
                    .append(fieldInfo.getColumn())
                    .append("`=#{item.")
                    .append(fieldInfo.getProperty())
                    .append("},</if>");
        }
        sb.append("\n\t\t</set>");
        sb.append("\n\t\tWHERE id =#{item.id}");
        sb.append("\n\t</foreach>");
        sb.append("\n</script>");
        System.out.println(sb);
        return sb.toString();
    }

    public static <T extends HasKey<K>> String updateBatchListByKey(Collection<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Class<? extends HasKey> clazz = list.iterator().next().getClass();

        TableInfo table = SqlHelper.table(clazz);
        StringBuilder sb = new StringBuilder();
        sb.append("<script>\n\t<foreach collection='list' item='item' separator=';'>")
                .append("\n\t\tUPDATE ")
                .append(table.getTableName())
                .append("\n\t\t<set>");
        List<TableFieldInfo> fieldList = table.getFieldList();
        for (TableFieldInfo fieldInfo : fieldList) {
            sb.append("\n\t\t\t<if test='item.")
                    .append(fieldInfo.getProperty())
                    .append(" != null'>`")
                    .append(fieldInfo.getColumn())
                    .append("`=#{item.")
                    .append(fieldInfo.getProperty())
                    .append("},</if>");
        }
        sb.append("\n\t\t</set>");
        sb.append("\n\t\tWHERE id =#{item.id}");
        sb.append("\n\t</foreach>");
        sb.append("\n</script>");
        System.out.println(sb);
        return sb.toString();
    }

    /**
     * 通过 Lambda 字段表达式获取字段对应数据库列名
     *
     * @param field Lambda 字段
     * @return 数据库列名
     */
    public static <T, R> String getColumn(SFunction<T, R> field) {
        LambdaMeta meta = LambdaUtils.extract(field);
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(meta.getInstantiatedClass());
        return columnMap.get(LambdaUtils.formatKey(fieldName)).getColumn();
    }

    /**
     * 设置查询字段
     *
     * @param select 查询字段
     */
    public static void setSelect(LambdaQueryWrapper<?> queryWrapper, String select) {
        try {
            if (selectField.get(queryWrapper) instanceof SharedString ss) {
                ss.setStringValue(select);
                return;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        throw new UnsupportedOperationException("不支持手动设置查询字段");
    }

    @SneakyThrows
    private static <T> Set<TableFieldInfo> getColumns(Collection<T> users, TableInfo tableInfo) {
        Set<TableFieldInfo> columns = new HashSet<>();
        for (T t : users) {
            for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                Object value = fieldInfo.getField().get(t);
                if (value == null) {
                    continue;
                }
                columns.add(fieldInfo);
            }
        }
        return columns;
    }

    public static void main(String[] args) {
        System.out.println(SqlUtils.selectField);
    }
}
