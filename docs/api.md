# 答测 API 概览

基础路径：`/api`

认证：除 `POST /auth/login` 外，请求头携带 `Authorization: Bearer <token>`。

## 账号

- `POST /auth/login` 微信登录。开发期可直接传 `openId`。
- `POST /auth/register` 注册角色，`role=CREATOR/PARTICIPANT`，注册后不可更改。
- `PUT /auth/profile` 修改昵称、头像。

## 空间

- `GET /spaces` 按角色返回我创建/我加入的空间。
- `POST /spaces` 创建者创建空间。
- `PUT /spaces/{id}` 创建者编辑空间。
- `POST /spaces/{id}/archive` 创建者归档空间。

## 邀请码

- `GET /spaces/{spaceId}/invites` 查看空间邀请码。
- `POST /spaces/{spaceId}/invites` 生成邀请码，默认 7 天、50 次。
- `POST /invites/{inviteId}/disable` 手动失效。
- `POST /invites/{inviteId}/delete` 删除邀请码记录。
- `POST /invites/join` 参与者凭邀请码加入空间。

## 题目套

- `GET /spaces/{spaceId}/quizzes` 题目套列表。参与者只看到已发布。
- `POST /quizzes` 创建草稿题目套。
- `PUT /quizzes/{quizId}` 编辑草稿。
- `GET /quizzes/{quizId}` 题目与选项详情。
- `POST /quizzes/{quizId}/publish` 发布。
- `POST /quizzes/{quizId}/close` 关闭。

## 作答与结果

- `POST /quizzes/{quizId}/attempts` 提交答案并自动判分。
- `GET /attempts/{attemptId}` 查看成绩和每题详情。

## 排行榜

- `GET /quizzes/{quizId}/ranking` 单套题排行。
- `GET /spaces/{spaceId}/ranking` 空间累计排行。
