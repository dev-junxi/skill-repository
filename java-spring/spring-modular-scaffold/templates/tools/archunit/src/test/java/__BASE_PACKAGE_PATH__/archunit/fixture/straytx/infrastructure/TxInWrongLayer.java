package __BASE_PACKAGE__.archunit.fixture.straytx.infrastructure;

import org.springframework.transaction.annotation.Transactional;

/** 违规：@Transactional 出现在 application 之外 */
@Transactional
public class TxInWrongLayer {}
