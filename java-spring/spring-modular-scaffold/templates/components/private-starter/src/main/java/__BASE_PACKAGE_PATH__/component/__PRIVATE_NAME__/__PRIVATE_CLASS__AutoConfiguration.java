package __BASE_PACKAGE__.component.__PRIVATE_NAME__;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * 私有组件 {@code __PRIVATE_ARTIFACT__} 的装配入口。
 *
 * <p><b>脚手架只做了接线，装配要你自己写。</b>
 *
 * <p>已经替你接好的（这些漏了会静默失效，所以必须自动化）：
 *
 * <ul>
 *   <li>依赖已加进 parent 的 dependencyManagement 与本 module 的 pom
 *   <li>本 module 已加进根 pom 的 {@code <modules>}
 *   <li>本 module 已加进 archunit module 的 {@code <dependencies>}——
 *       <b>漏了这条，所有架构规则对本模块形同虚设，而测试仍然全绿</b>
 *   <li>文档索引与依赖快照已刷新
 * </ul>
 *
 * <p>接下来要你做的两件事：
 *
 * <ol>
 *   <li>在这里声明 {@code __PRIVATE_ARTIFACT__} 需要的 bean
 *   <li>把 AGENTS.md 里三处 TODO 填掉（{@code summary} 不填，文档卡口会红）
 * </ol>
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "__ARTIFACT_PREFIX__.__PRIVATE_NAME__",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class __PRIVATE_CLASS__AutoConfiguration {

    // TODO 在这里声明 __PRIVATE_ARTIFACT__ 需要的 bean。
    //
    // 脚手架刻意不猜你这个库该怎么装配：猜错了会编译通过但运行时行为诡异，
    // 留空则会在第一次用它时立刻 NoSuchBeanDefinitionException——失败得更响亮，
    // 也更容易定位。
}
