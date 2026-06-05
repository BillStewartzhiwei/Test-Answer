# Dace MVP

This workspace contains the initial scaffold for the Dace quiz miniapp MVP.

## Decisions locked for MVP

- Question types: choice questions only (`single`, `multiple`, `judge`).
- Attempt rule: one participant can submit each quiz only once.
- Leaderboard privacy: participants do not see other participants' exact scores.
- WeChat identity: miniapp `openid` only for now.
- Creator signup: no approval flow in MVP.

## Structure

- `dace-server`: Spring Boot 2.7 + MyBatis-Plus backend.
- `dace-miniapp`: native WeChat miniapp frontend scaffold.

## Backend

Configure MySQL in `dace-server/src/main/resources/application.yml`, then create tables with:

```sql
source dace-server/src/main/resources/sql/schema.sql;
```

Run when JDK and Maven are available:

```bash
cd dace-server
mvn spring-boot:run
```

## Miniapp

Open `dace-miniapp` in WeChat DevTools. Install/build npm in DevTools so `@vant/weapp` is available.
