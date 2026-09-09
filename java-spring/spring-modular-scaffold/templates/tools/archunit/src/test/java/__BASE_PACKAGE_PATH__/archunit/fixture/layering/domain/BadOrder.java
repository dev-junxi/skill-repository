package __BASE_PACKAGE__.archunit.fixture.layering.domain;

import __BASE_PACKAGE__.archunit.fixture.layering.application.OrderAppService;

/** 违规：domain 反向依赖 application */
public class BadOrder {
    private OrderAppService service;

    public OrderAppService service() {
        return service;
    }
}
