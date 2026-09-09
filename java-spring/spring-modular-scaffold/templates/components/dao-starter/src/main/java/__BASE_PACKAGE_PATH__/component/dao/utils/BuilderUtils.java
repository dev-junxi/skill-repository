package __BASE_PACKAGE__.component.dao.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import __BASE_PACKAGE__.contract.response.PageData;
import com.msl.model.builder.ModelBuilder;
import com.msl.model.builder.context.impl.SimpleBuildContext;
import com.msl.view.mapper.ViewMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author wanglq
 * Date 2023/3/24
 * Time 14:27
 */
public class BuilderUtils {
    public static <V, T> List<V> buildView(Collection<T> list, ModelBuilder modelBuilder, ViewMapper viewMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        SimpleBuildContext context = new SimpleBuildContext();
        modelBuilder.buildMulti(list, context);
        return viewMapper.map(list, context);
    }

    public static <V, T> Page<V> buildView(Page<T> page, ModelBuilder modelBuilder, ViewMapper viewMapper) {
        if (page == null) {
            return null;
        }
        SimpleBuildContext context = new SimpleBuildContext();
        modelBuilder.buildMulti(page.getRecords(), context);
        Page<V> ans = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        ans.setRecords(viewMapper.map(page.getRecords(), context));
        return ans;
    }

    public static <V, T> IPage<V> buildView(IPage<T> page, ModelBuilder modelBuilder, ViewMapper viewMapper) {
        if (page == null) {
            return null;
        }
        SimpleBuildContext context = new SimpleBuildContext();
        modelBuilder.buildMulti(page.getRecords(), context);
        Page<V> ans = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        ans.setRecords(viewMapper.map(page.getRecords(), context));
        return ans;
    }

    public static <V, T> PageData<V> buildView(PageData<T> page, ModelBuilder modelBuilder, ViewMapper viewMapper) {
        if (page == null) {
            return null;
        }
        SimpleBuildContext context = new SimpleBuildContext();
        modelBuilder.buildMulti(page.records(), context);
        // __ARTIFACT_PREFIX__ 的 PageData 是不可变 record（契约的一部分，见 __ARTIFACT_PREFIX__-contract），
        // 不同于来源实现那个带 setter 的可变类，所以这里构造新实例而非逐个 set
        return PageData.of(viewMapper.map(page.records(), context), page.total(), page.current(), page.size());
    }

    public static <V, T> V buildView(T t, ModelBuilder modelBuilder, ViewMapper viewMapper) {
        if (t == null) {
            return null;
        }
        SimpleBuildContext context = new SimpleBuildContext();
        modelBuilder.buildSingle(t, context);
        return viewMapper.map(t, context);
    }
}
