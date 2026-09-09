package __BASE_PACKAGE__.archunit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/**
 * 每条规则两个测试：真实代码必须通过，违规 fixture 必须让它抛 AssertionError。
 *
 * <p><b>后者才是关键。</b>只有前者的话，一个匹配不到任何类的包表达式会让 check() 空集通过、
 * 测试永远绿，而实际什么都没管住——那比没有规则更糟，因为它提供虚假的安全感。
 */
class ArchRulesTest {

    /** 真实生产代码：排除测试类，所以 fixture 不会被扫进来 */
    private static final JavaClasses PRODUCTION = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            // agentv2 按管线分包而非 DDD，豁免 R2/R3（spec §7 R8）
            .withImportOption(location -> !location.contains("/agentv2/"))
            .importPackages("__BASE_PACKAGE__");

    private static JavaClasses fixture(String subPackage) {
        return new ClassFileImporter().importPackages("__BASE_PACKAGE__.archunit.fixture." + subPackage);
    }

    /**
     * 断言规则在违规样本上会红，<b>且是因为真实违规而红</b>。
     *
     * <p>只断言「抛了 AssertionError」是不够的：ArchUnit 在规则一个类都没匹配到时
     * 也会抛同类异常。于是一个包匹配表达式写错的规则（比如 {@code ..domain..} 打成
     * 别的）在 fixture 上照样"红"，测试全绿，而规则对生产代码完全空转。
     *
     * <p>所以必须再断言失败信息里**点名了预期的违规类**——这同时排除了
     * 「没抛」与「因错误原因抛」两种情况。
     *
     * @param expectedViolatingClass 预期被点名的违规类简单类名
     */
    private static void assertRuleFires(ArchRule rule, JavaClasses violating, String expectedViolatingClass) {
        assertThatThrownBy(() -> rule.check(violating))
                .as("规则必须在违规样本上抛异常，否则它形同虚设")
                .isInstanceOf(AssertionError.class)
                .as("失败信息必须点名 %s——否则可能是「一个类都没匹配到」而非真实违规", expectedViolatingClass)
                .hasMessageContaining(expectedViolatingClass);
    }

    /**
     * 生产代码里是否存在含该片段的包。
     *
     * <p>用途：DDD 分层规则（R1/R2/R3/R4/R9）针对的是业务模块的包结构，而脚手架阶段
     * 业务模块尚未落地。ArchUnit 默认 {@code failOnEmptyShould=true}，规则匹配到空集时会
     * <b>直接报错</b>而不是假绿——这是它的正确行为，不该用 {@code allowEmptyShould(true)} 绕过。
     *
     * <p>所以这里改用 JUnit 假设：包不存在时跳过，<b>第一个业务模块落地后这些断言自动生效</b>，
     * 不需要任何人记得回来打开开关。
     */
    private static boolean hasPackageContaining(String fragment) {
        return PRODUCTION.stream().anyMatch(c -> c.getPackageName().contains(fragment));
    }

    // ── R1

    @Test
    void r1_passes_on_production() {
        // 注意不能只判 ".web"——那会命中组件的 __BASE_PACKAGE__.component.web，
        // 而 R1 针对的是业务模块的 __BASE_PACKAGE__.server.<模块>.web
        assumeTrue(
                PRODUCTION.stream()
                        .anyMatch(c -> c.getPackageName().startsWith("__BASE_PACKAGE__.server.")
                                && c.getPackageName().contains(".web")),
                "尚无业务模块的 server.*.web 包——业务模块落地后本断言自动生效");
        ArchRules.r1_webModulesDoNotDependOnEachOther().check(PRODUCTION);
    }

    // ── R2

    @Test
    void r2_passes_on_production() {
        assumeTrue(hasPackageContaining(".domain"), "尚无 domain 包——业务模块落地后本断言自动生效");
        ArchRules.r2_domainDoesNotDependOnOuterLayers().check(PRODUCTION);
    }

    @Test
    void r2_fires_on_violation() {
        assertRuleFires(ArchRules.r2_domainDoesNotDependOnOuterLayers(), fixture("layering"), "BadOrder");
    }

    // ── R3

    @Test
    void r3_passes_on_production() {
        assumeTrue(hasPackageContaining(".domain"), "尚无 domain 包——业务模块落地后本断言自动生效");
        ArchRules.r3_domainHasNoFrameworkDependencies().check(PRODUCTION);
    }

    @Test
    void r3_fires_on_violation() {
        assertRuleFires(ArchRules.r3_domainHasNoFrameworkDependencies(), fixture("dirtydomain"), "AnnotatedEntity");
    }

    // ── R4

    @Test
    void r4_passes_on_production() {
        assumeTrue(hasPackageContaining(".controller"), "尚无 controller 包——业务模块落地后本断言自动生效");
        ArchRules.r4_controllerDoesNotTouchMapper().check(PRODUCTION);
    }

    @Test
    void r4_fires_on_violation() {
        assertRuleFires(ArchRules.r4_controllerDoesNotTouchMapper(), fixture("fatcontroller"), "LeakyController");
    }

    // ── R5

    @Test
    void r5_passes_on_production() {
        ArchRules.r5_transactionalOnlyInApplication().check(PRODUCTION);
    }

    @Test
    void r5_fires_on_violation() {
        assertRuleFires(ArchRules.r5_transactionalOnlyInApplication(), fixture("straytx"), "TxInWrongLayer");
    }

    // ── R6

    @Test
    void r6_passes_on_production() {
        ArchRules.r6_componentsDoNotDependOnServer().check(PRODUCTION);
    }

    // ── R7

    @Test
    void r7_passes_on_production() {
        ArchRules.r7_contractHasNoSpring().check(PRODUCTION);
    }

    @Test
    void r7_fires_on_violation() {
        assertRuleFires(
                ArchRules.r7_contractHasNoSpring(),
                new ClassFileImporter().importPackages("__BASE_PACKAGE__.contract.fixturebad"), "SpringyDto");
    }

    // ── R9

    @Test
    void r9_passes_on_production() {
        assumeTrue(hasPackageContaining(".domain"), "尚无 domain 包——业务模块落地后本断言自动生效");
        ArchRules.r9_domainDoesNotDependOnAnyApiModule().check(PRODUCTION);
    }

    // ── R11

    // @if auth
    @Test
    void r11_passes_on_production() {
        ArchRules.r11_saTokenIsConfinedToAuth().check(PRODUCTION);
    }
    // @endif

    // @if auth
    @Test
    void r11_fires_on_violation() {
        assertRuleFires(ArchRules.r11_saTokenIsConfinedToAuth(), fixture("satokenleak"), "BusinessServiceUsingStpUtil");
    }
    // @endif

    // ── R12

    @Test
    void r12_passes_on_production() {
        ArchRules.r12_noRoutingDataSource().check(PRODUCTION);
    }

    @Test
    void r12_fires_on_violation() {
        assertRuleFires(ArchRules.r12_noRoutingDataSource(), fixture("routingds"), "EnvRoutingDataSource");
    }

    /**
     * 防空转：确认 PRODUCTION 真的扫到了类，而不是空集。
     *
     * <p>没有这条，上面所有 *_passes_on_production 都可能是在对空集做断言——恒真。
     */
    @Test
    void production_import_is_not_empty() {
        org.assertj.core.api.Assertions.assertThat(PRODUCTION.size())
                .as("ArchUnit 必须真的扫到生产类，空集会让所有正向断言恒真")
                .isGreaterThan(10);
    }
}
