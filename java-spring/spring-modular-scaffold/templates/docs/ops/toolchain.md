# 工具链

## Serena（语义检索 MCP）

**解决什么问题**：agent 需要知道"有哪些类、哪个方法在哪"，但这类信息写进静态文档必然漂移。Serena 基于 LSP 做符号级检索，让 agent 按需读符号而不是整文件，省 token 也更准。

### 装配

```bash
claude mcp add serena -- uvx --from git+https://github.com/oraios/serena \
  serena-mcp-server --context ide-assistant --project "$(pwd)"
```

首次启动会建索引，耗时取决于代码量。

### 注意

- `.serena/` 已在 `.gitignore` 里，索引缓存不入库
- Serena 会在 `.serena/memories/` 自动写 md 文件，**那不是项目文档**，不要把它当作 `AGENTS.md` 的替代或补充
- 索引过期时删掉 `.serena/` 重建

## 其它

| 工具 | 用途 | 位置 |
|---|---|---|
| spotless | 代码格式化。风格规则不写进上下文文件，交给工具链 | `__ARTIFACT_PREFIX__-parent` 的 pluginManagement |
| springdoc | API 文档生成，不手写路径表 | `__ARTIFACT_PREFIX__-web-spring-boot-starter` |
| OWASP dependency-check | 依赖漏洞扫描（[ADR-0008](../adr/ADR-0008-spring-boot-3-5-16.md) 强制项） | CI |
| Testcontainers | 集成测试的干净数据库 | `__ARTIFACT_PREFIX__-dao-spring-boot-starter` 及各业务模块 test scope |

## 绝不写进文档的四类

| 类别 | 交给谁 |
|---|---|
| API 路径表 | springdoc / OpenAPI 生成 |
| 类、方法清单 | IDE + Serena 语义检索 |
| 字段说明 | SQL 注释 + JavaDoc |
| 代码风格与命名 | spotless |

参考实现的 `modules-api-endpoints.md` 就是第一类，写完那天起就在烂。

## 待办

- **HikariCP 指标验证**：`__ARTIFACT_PREFIX__-app` 目前未配置数据源（脚手架阶段无业务模块），
  `/actuator/metrics` 里还看不到 `hikaricp.*`。第一个业务模块接入数据库时，
  必须验证这些指标真的暴露出来了——**语句执行快不代表接口快**，连接池排队时间
  必须可见，否则连接池打满时慢查询日志会干干净净而接口是慢的。

## CI 与卡口的强制力现状

| 层 | 强制力 | 说明 |
|---|---|---|
| `tools/hooks/pre-push` | 弱 | 三个 shell 卡口，约 1 秒。可被 `--no-verify` 绕过，只在本机 |
| GitHub Actions CI | 中 | 全量跑（含测试），失败会发邮件，但**拦不住合并** |
| branch protection | 强（**未启用**） | 私有仓库需 GitHub Pro。免费版下 branch protection 与 rulesets 两个 API 都返回 403 |

**要拿到真正的"跑不过就合不进去"，唯一的路是升 GitHub Pro**（约 $4/月），然后：

```bash
gh api -X PUT repos/Omniora/__PROJECT_NAME__/branches/master/protection --input - <<'JSON'
{"required_status_checks":{"strict":true,"contexts":["build"]},
 "enforce_admins":false,"required_pull_request_reviews":null,
 "restrictions":null,"allow_force_pushes":false,"allow_deletions":false}
JSON
```

开启后**必须造一个必然失败的 PR 验证它真的拦得住**——没被证明会拦人的保护不算保护，与本项目所有卡口同一条原则。
