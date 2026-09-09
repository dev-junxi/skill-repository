package __BASE_PACKAGE__.archunit.fixture.dirtydomain.domain;

import org.springframework.stereotype.Service;

/** 违规：domain 里出现 Spring 注解 */
@Service
public class AnnotatedEntity {}
