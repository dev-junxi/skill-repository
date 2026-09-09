package __BASE_PACKAGE__.contract.fixturebad;

import org.springframework.stereotype.Component;

/**
 * 违规：contract 包里出现 Spring 依赖。
 *
 * <p>包名必须落在 __BASE_PACKAGE__.contract.. 之下，R7 才会命中它。它在 test 源码树里，
 * 不会进生产 jar。
 */
@Component
public class SpringyDto {}
