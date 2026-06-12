# 答测

答测是一个微信小程序答题系统 MVP，覆盖「注册 → 空间 → 出题 → 邀请 → 答题 → 成绩 → 排行榜」完整闭环。

## 目录

- `backend/`：Spring Boot 2.7 + MyBatis-Plus + JWT 后端。
- `miniprogram/`：微信小程序原生 WXML/WXSS/JS。
- `docs/api.md`：接口概览。

## 先跑无后端小程序版

当前小程序已内置本地 mock API，数据写入微信本地缓存，不需要启动后端。

1. 用微信开发者工具打开 `miniprogram/`。
2. 登录页输入测试 `openId`，例如 `creator001`。
3. 注册为创建者，创建空间、题目和邀请码。
4. 重新编译或清缓存后换一个 `openId`，例如 `user001`，注册为参与者并用邀请码加入答题。

需要清空本地数据时，在开发者工具 Storage 面板删除 `mockDb`、`token`、`user`、`lastResult`。

## 后端版运行

1. 安装 JDK 8+、Maven、MySQL。
2. 在 MySQL 执行 `backend/src/main/resources/schema.sql`。
3. 修改 `backend/src/main/resources/application.yml` 中的数据库账号、密码和 `jwt.secret`。
4. 启动服务：

```bash
cd backend
mvn spring-boot:run
```

默认接口地址：`http://localhost:8080/api`

当前小程序默认使用本地 mock API。如果要切回后端版，把 `miniprogram/utils/request.js` 替换为网络请求实现，并请求上述接口地址。

## 小程序运行

1. 用微信开发者工具打开 `miniprogram/`。
2. 先登录，注册为「创建者」或「参与者」。

## 主要功能

- 创建者：创建/编辑/归档空间，生成邀请码，创建题目套，编辑题目，发布/关闭，查看排行。
- 参与者：输入邀请码加入空间，作答已发布题目，查看成绩和排行。
- 排名规则：得分高优先，同分按用时短优先。

## 数据表

共 9 张表：

- `users`
- `spaces`
- `invite_codes`
- `space_members`
- `quizzes`
- `questions`
- `options`
- `quiz_attempts`
- `answers`
