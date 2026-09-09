# CI 与卡口

## 两个 workflow

| workflow | 触发 | 耗时 | 阻断 |
|---|---|---|---|
| `ci.yml` | 每次 push / PR | ~3.5 分钟 | 是 |
| `security-scan.yml` | 每周一 UTC 02:00 + 手动 | ~20 分钟 | 否 |

依赖漏洞扫描单独拆出来，是因为 NVD 数据库要下载数年的 CVE 数据，实测单次 20 分钟以上。**挂在主 CI 上会让每次提交都等二十分钟——那样的 CI 人会开始绕过，所有卡口就都白设了。** CVE 数据是天级更新，不需要每 commit 重扫。

## 私服凭证

若 parent 或某个依赖在私服上（选 `standalone` parent、或选了持久层时），runner 需要凭证才能拉取。两个仓库级 secret：

- `MAVEN_REPO_USERNAME`
- `MAVEN_REPO_PASSWORD`

**必须是仓库级而非组织级**——免费版不允许组织 secret 下发给私有仓库。

## ⚠️ 已知缺口：没有服务端分支保护

GitHub 免费版**不支持私有仓库的 branch protection 与 rulesets**（两者都返回 `403 Upgrade to GitHub Pro`）。

后果：**CI 红了也拦不住 push 到 master**。CI 目前只是"事后通知"，不是卡口的完整形态。

三条出路：

| 方案 | 代价 |
|---|---|
| GitHub Pro | 约 $4/月，解锁 branch protection + rulesets |
| 仓库转公开 | 代码公开 |
| 本地 pre-push hook（当前兜底） | 可被 `--no-verify` 绕过，且只在装了 hook 的机器上生效 |

### 安装本地 hook

```bash
git config core.hooksPath tools/hooks   # 每台机器 clone 后跑一次
```

用 `core.hooksPath` 而不是往 `.git/hooks/` 建软链：hook 本体在版本控制里，改一次全员生效，
且不会出现「某台机器的软链指向已删除的旧路径」这类只在那台机器上复现的问题。

它只跑三个秒级 shell 卡口（docs-index / module-graph / check-migrations），不跑测试——跑全量测试要几分钟，那样的 hook 最终会被人自己关掉。测试与 ArchUnit 仍由 CI 执行。

**这不等价于分支保护**，只是让诚实的疏忽在本地就被发现。团队规模一旦超过你一个人，就该升 Pro。
