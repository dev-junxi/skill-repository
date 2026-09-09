# 模块依赖规则

结论在 [ADR-0002](../adr/ADR-0002-cross-module-via-api-module.md)，这里是展开说明。

## 四条规则

1. `X-web` 可依赖任意 `Y-api`
2. `X-web` **绝不可**依赖 `Y-web` —— Maven 编译期硬挡
3. `X-api` 只能依赖 `__ARTIFACT_PREFIX__-contract`，**不得依赖任何其他 `Y-api`** —— 否则 api 之间会传染成网，最后跟没拆一样。若 `X-api` 的 DTO 需要 `Y-api` 的类型，那个类型就该下沉到 `contract`
4. `X` 的 `domain` 包不得 import 任何 `..api..` —— 领域层只认识自己

## 调用怎么落地

以 care 要用 ai 的文案生成为例：

```
care-web/domain/port/CopyComposerPort              care 用自己的语言定义端口
        ↑ implements
care-web/infrastructure/AiCopyComposerAdapter      唯一知道 ai 存在的类
        ↓ @Autowired
ai-api/AiGenerateService                           ai 的对外契约（接口）
        ↑ implements
ai-web/application/AiGenerateServiceImpl           @Service
```

三个收益：**domain 保持纯净**（只认识自己的 Port）；**跨模块耦合被压缩到 infrastructure 的一个类里**；**app 回归纯组装**（启动类 + 全局配置，没有业务胶水）。

未来拆服务时 `AiGenerateService` 换成 Feign/Dubbo 客户端，`AiCopyComposerAdapter` 零改动，care 整个模块零改动。**这就是 api module 的全部价值所在。**

## ⚠️ 双向依赖会悄悄长出来，现有工具都抓不到

`A-web → B-api` 与 `B-web → A-api` 同时存在时：

- **Maven 不认为这是循环** —— api 和 web 是不同 artifact
- **ArchUnit 的包循环检测也抓不到** —— `a.api` 和 `a.web` 是不同包

所以它能一路长到没人能拆的程度。

对策是 `tools/module-graph.sh` 的快照 + CI diff：任何跨模块依赖的新增都会在 PR 里显形。**这是目前唯一能发现它的手段。**

## 事务边界

- **跨模块调用不得依赖调用方的事务**，被调方管自己的事务。理由：拆成 RPC 后事务必然断裂，现在依赖它就是埋雷
- 跨模块的**写**操作优先用领域事件（异步），而不是同步调用
- 必须同步写的，被调接口必须**幂等** —— RPC 会重试

| 场景 | 方式 |
|---|---|
| 需要返回值、查询 | 同步调 `Y-api` 接口 |
| 通知、触发副作用、不关心结果 | 领域事件（见 [ADR-0013](../adr/ADR-0013-domain-event-without-application-event.md)） |
