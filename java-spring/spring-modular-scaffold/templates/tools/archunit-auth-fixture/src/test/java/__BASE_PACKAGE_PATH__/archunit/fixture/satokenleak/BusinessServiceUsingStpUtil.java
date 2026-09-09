package __BASE_PACKAGE__.archunit.fixture.satokenleak;

import cn.dev33.satoken.stp.StpUtil;

/** 违规：auth 之外的地方直接用 StpUtil */
public class BusinessServiceUsingStpUtil {
    public Object currentId() {
        return StpUtil.getLoginId();
    }
}
