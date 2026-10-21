# skill-repository

按能力分层的 skill 仓库。Agent 可以搜索、安装、更新、上传、删除。

## 结构

```
<分类>/<skill-name>/SKILL.md    每个 skill 一个目录
categories.json                  分类定义（label + emoji）
registry.json                    索引，由脚本推导，禁止手改
tools/build-registry.py          生成/校验索引
tools/skill-repo.sh              CRUD 工具
```

**`registry.json` 是被推导的，不是被维护的。** 加减 skill 只需增删目录，跑一次
`tools/build-registry.py`。手写清单是第二份真实来源，必然与实际目录漂移。

只保留已有 skill 的分类目录，不预建空分类和占位文件。

## 已有技能

| Skill | 用途 |
|---|---|
| [module-acceptance-testing](testing-quality/module-acceptance-testing/SKILL.md) | 为业务模块设计、实现和复测分级验收用例，记录调用证据、生成报告并清理测试资源；附 Java/Maven 参考和复用模板。 |
| [spring-modular-scaffold](java-spring/spring-modular-scaffold/SKILL.md) | 生成带模块边界、架构规则和文档约束的 Spring Boot 服务骨架。 |

## 给 Agent 用

```bash
tools/skill-repo.sh list [分类]           # 列出
tools/skill-repo.sh search <关键词>       # 搜索
tools/skill-repo.sh install <name>        # 安装到本地
tools/skill-repo.sh update <name>         # 更新
tools/skill-repo.sh uninstall <name>      # 只删本地
tools/skill-repo.sh upload <目录> <分类>   # 上传（走 PR）
tools/skill-repo.sh remove <name>         # 从仓库删除（走 PR）
```

安装目录自动识别（Claude Code / Hermes / OpenClaw），可用 `SKILLS_DIR` 覆盖。

**改仓库内容的两个操作（upload / remove）一律走分支 + PR，不直推 main**——
skill 是会被很多 agent 直接执行的代码，误推一个坏版本的影响面比普通代码大。

## 分类

见 `categories.json`。新增 skill 时优先使用现有分类；确需新分类时，添加分类定义和同名目录，并直接放入实际 skill，然后重新生成 registry。不要为未来可能用到的分类保留空目录。

## 校验

```bash
python3 tools/build-registry.py --check
```

CI 会跑这条。它同时检查：每个 skill 有 `SKILL.md`、frontmatter 有 `description`、
目录名与 `name` 一致、没有游离在 `categories.json` 之外的目录（那样的目录下
skill 永远不会被收录，是静默失效）。
