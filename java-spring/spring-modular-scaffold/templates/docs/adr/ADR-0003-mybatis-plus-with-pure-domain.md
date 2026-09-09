# ADR-0003: 持久层 MyBatis-Plus + 手写 SQL，domain 保持纯净

Status: Accepted (2026-09-08)

Context:
  参考实现里有大量带守卫的 UPDATE（`WHERE ... AND state = ?` + 检查 rows_affected）、
  批量认领、信封加密列，这些要求 SQL 完全可控。JPA 做得到但要与 Hibernate 反复搏斗。

Decision:
  MyBatis-Plus + 手写 SQL。domain 层是纯 POJO，Repository 接口定义在 domain、
  实现放 infrastructure，PO 与 domain 实体之间用 MapStruct 转换。

Consequences:
  - MUST domain 包不得 import org.springframework.. / com.baomidou.. /
    cn.dev33.satoken.. / jakarta.persistence..（ArchUnit R3）
  - MUST @Transactional 只写在 application 层（ArchUnit R5）
  - 代价：多一层 PO↔domain 转换代码。换来的是 domain 能脱离框架直接 new 出来测试

Verify:
  mvn -pl tools/__ARTIFACT_PREFIX__-archunit test   # R3、R5
