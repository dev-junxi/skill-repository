package __BASE_PACKAGE__.component.dao.config;

import org.mybatis.spring.annotation.MapperScan;

/**
 * @author wanglq
 * Date 2024/4/16
 * Time 20:34
 */
@MapperScan(basePackages = {"__BASE_PACKAGE__.component.dao.mapper"})
public class BatchMapperConfig {}
