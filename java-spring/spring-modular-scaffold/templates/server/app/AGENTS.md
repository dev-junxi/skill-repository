---
module: __ARTIFACT_PREFIX__-app
type: server
summary: 唯一组装点——启动类、全局配置、健康端点，不含业务逻辑
---

# __ARTIFACT_PREFIX__-app

## 职责

- Spring Boot 启动类（`scanBasePackages = "__BASE_PACKAGE__"`）
- 全局配置 `application.yml` / `logback-spring.xml`
- 给 LB 用的 `/v1/health`

## 铁律

**这里不写业务逻辑。** 它是唯一被允许知道所有模块存在的地方，但跨模块协作的落点是**调用方 infrastructure 里的 Adapter**，不是这里——app 里堆胶水会让它慢慢变成第二个业务层。

## 本地启动

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21 mvn install -DskipTests
JAVA_HOME=/opt/homebrew/opt/openjdk@21 mvn -pl server/__ARTIFACT_PREFIX__-app spring-boot:run
```

**不能用 `-am`**：那会让 `spring-boot:run` 也作用到根聚合 pom，报 `Unable to find a suitable main class`。

## 多实例注意

`server.shutdown: graceful` 不能删——滚动发布时停机实例上正在处理的请求（尤其 SSE 长连接）会被砍断。
