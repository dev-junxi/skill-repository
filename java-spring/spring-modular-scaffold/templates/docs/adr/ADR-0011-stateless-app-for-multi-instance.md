# ADR-0011: 应用层零状态，支持单体多实例

Status: Accepted (2026-09-08)

Context:
  架构必须支持同一应用跑多个实例（水平复制 + 负载均衡）。这不等于微服务——
  仍是单体，只是复制若干份。任何"只存在于某个实例内存里、且下次请求需要读到"
  的状态都会失效，且失效方式往往是静默的（限流阈值被放大 N 倍，不报错）。

Decision:
  应用层零状态。会话走 Sa-Token JWT Stateless（服务端零存储，ADR-0009）；
  应用层限流挪到 Nginx 的 limit_req_zone；定时任务按 ADR-0012 分两类处理；
  **当前不引入 Redis**。

Consequences:
  - MUST server.shutdown=graceful，否则滚动发布会砍断正在处理的请求（尤其 SSE）
  - MUST 连接池上限按"实例数 × maximum-pool-size"倒推，别撞 MySQL max_connections
  - MUST NOT 使用 sticky session
  - 静态资源（角色卡 / preset）打进 jar，多实例天然一致
  - **引入 Redis 的四个触发条件**（出现任一即引入，届时另开 ADR）：
      1. 需要服务端强制下线 / token 撤销
      2. 需要应用层精确的分布式限流（Nginx 粒度不够用）
      3. 需要跨实例共享缓存
      4. 需要分布式锁且不愿用 DB 表
  - 在这四项出现之前不引——每多一个中间件就多一个要监控、备份、故障排查的东西。
    不列这个清单，"以后再说"会变成"永远不知道什么时候该做"

Verify:
  grep -rn "HttpSession\|@SessionAttributes" server/ components/   # 应无匹配
  grep -n "shutdown: graceful" server/__ARTIFACT_PREFIX__-app/src/main/resources/application.yml
