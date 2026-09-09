package __BASE_PACKAGE__.archunit.fixture.routingds;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/** 违规：正是 ADR-0001 禁止的那种"按环境切库" */
public class EnvRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return "prod";
    }
}
