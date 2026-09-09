package __BASE_PACKAGE__.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.lang.ArchRule;

/**
 * 架构铁律的可执行版本。规则编号与 spec §7 对应。
 *
 * <p>每条规则都必须在 {@link ArchRulesTest} 里配一个"违规 fixture 会让它抛异常"的测试——
 * <b>一条从未红过的规则等于没有规则</b>。写错包匹配表达式会让 check() 空集通过、永远绿，
 * 而实际什么都没管住。
 *
 * <p>R8 不是规则而是豁免（{@code ..agentv2..} 按管线分包不套 DDD），在导入时排除；
 * R10 与 R1 是同一条断言且 Maven 已在编译期硬挡，不重复定义。
 */
public final class ArchRules {

    private ArchRules() {}

    /** R1：X-web 不得依赖 Y-web —— 跨模块只能经由对方的 -api（ADR-0002） */
    public static ArchRule r1_webModulesDoNotDependOnEachOther() {
        return noClasses()
                .that()
                .resideInAPackage("..server.(*).web..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..server.(*).web..")
                .because("跨模块依赖只允许经由对方的 -api module（ADR-0002）");
    }

    /** R2：domain 不得依赖 application / infrastructure / controller */
    public static ArchRule r2_domainDoesNotDependOnOuterLayers() {
        return noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..application..", "..infrastructure..", "..controller..")
                .because("依赖方向是 controller -> application -> domain <- infrastructure，domain 不依赖任何人");
    }

    /** R3：domain 包内零框架依赖 */
    public static ArchRule r3_domainHasNoFrameworkDependencies() {
        return noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..", "com.baomidou..", "cn.dev33.satoken..", "jakarta.persistence..")
                .because("domain 必须是能脱离框架直接 new 出来测试的纯 POJO（ADR-0003）");
    }

    /** R4：Controller 不得直接依赖 Mapper */
    public static ArchRule r4_controllerDoesNotTouchMapper() {
        return noClasses()
                .that()
                .resideInAPackage("..controller..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..mapper..", "..infrastructure.persistence..")
                .because("Controller 必须经由 application 层，不能直连持久化");
    }

    /** R5：@Transactional 只出现在 application 层 */
    public static ArchRule r5_transactionalOnlyInApplication() {
        return noClasses()
                .that()
                .resideOutsideOfPackage("..application..")
                .should()
                .beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("事务边界统一在 application 层，散落各层会导致边界不可预测");
    }

    /** R6：components 与 contract 不得依赖 server */
    public static ArchRule r6_componentsDoNotDependOnServer() {
        return noClasses()
                .that()
                .resideInAnyPackage("__BASE_PACKAGE__.component..", "__BASE_PACKAGE__.contract..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("__BASE_PACKAGE__.server..")
                .because("组件是被业务复用的，反向依赖会让组件无法独立抽出");
    }

    /** R7：contract 不得 import Spring */
    public static ArchRule r7_contractHasNoSpring() {
        return noClasses()
                .that()
                .resideInAPackage("__BASE_PACKAGE__.contract..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("org.springframework..")
                .because("contract 是未来的跨服务协议包，必须零 Spring 依赖（ADR-0013）");
    }

    /** R9：domain 不得 import 任何模块的 -api 包 */
    public static ArchRule r9_domainDoesNotDependOnAnyApiModule() {
        return noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("__BASE_PACKAGE__.server..api..")
                .because("领域层只认识自己定义的 Port，跨模块调用的落点在 infrastructure 的 Adapter");
    }

    // @if auth
    /** R11：Sa-Token 只允许出现在 auth 组件与 auth 模块 */
    public static ArchRule r11_saTokenIsConfinedToAuth() {
        return noClasses()
                .that()
                .resideOutsideOfPackages("__BASE_PACKAGE__.component.auth..", "__BASE_PACKAGE__.server.auth..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("cn.dev33.satoken..")
                .because("Sa-Token 是可替换的实现细节；放任 StpUtil 蔓延到 domain 会让 R3 形同虚设（ADR-0009）");
    }
    // @endif

    /** R12：禁止用 AbstractRoutingDataSource 做环境隔离（ADR-0001） */
    public static ArchRule r12_noRoutingDataSource() {
        return noClasses()
                .should()
                .beAssignableTo("org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource")
                .because("环境隔离必须靠物理隔离：一个进程一个库一把密钥。" + "进程内按 Host 选库曾导致 test 用户 JWT 冒充 prod 同 ID 用户的 P0 事故（ADR-0001）");
    }
}
