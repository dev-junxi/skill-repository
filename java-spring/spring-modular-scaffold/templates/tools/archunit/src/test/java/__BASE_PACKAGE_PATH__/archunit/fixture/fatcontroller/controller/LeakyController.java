package __BASE_PACKAGE__.archunit.fixture.fatcontroller.controller;

import __BASE_PACKAGE__.archunit.fixture.fatcontroller.mapper.OrderMapper;

/** 违规：Controller 直连 Mapper */
public class LeakyController {
    private OrderMapper mapper;

    public OrderMapper mapper() {
        return mapper;
    }
}
