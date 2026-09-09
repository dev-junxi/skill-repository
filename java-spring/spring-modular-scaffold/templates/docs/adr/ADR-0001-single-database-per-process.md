# ADR-0001: 单库单进程环境隔离

Status: Accepted (2026-09-08)

Context:
  参考实现（Rust 版）曾在进程内按 Host 头选择数据库与 JWT 密钥，发生 P0 串号事故：
  test 环境用户的 JWT 在 prod 冒充了同 ID 的用户。根因是 token 里不含环境信息，
  对称密钥共用时验签必然跨环境通过。

Decision:
  一个进程只连一个库、只持一把签名密钥。test / prod 是两个独立进程 + 各自的
  application-{env}.yml，由 Nginx 按域名分流。

Consequences:
  - MUST NOT 使用 AbstractRoutingDataSource 做环境隔离。Java 里这个类现成可用，
    使同样的错误比 Rust 版更容易犯、看起来还更"专业"——由 ArchUnit R12 强制拦截
  - MUST secret 走环境变量占位，不提交进 yml
  - MUST 同一环境的所有实例共享同一份配置（多实例前提，见 ADR-0011）

Verify:
  mvn -pl tools/__ARTIFACT_PREFIX__-archunit test   # R12 会拦下任何 AbstractRoutingDataSource 子类
  grep -rn "AbstractRoutingDataSource" server/ components/   # 应无匹配
