# Day134：Vue3 登录页（Session + `fetch` Cookie）

## 交付（`oa-demo/src/main/webapp/web/`）

- **`login.html`**：Vue 3 CDN；**`POST …/api/login`** 使用 **`fetch(..., { credentials: 'include' })`**，便于浏览器保存 **`Set-Cookie: JSESSIONID`**。
- **`app.html`**：登录成功后跳转；读取 **`sessionStorage`** 中的登录摘要；按钮试调 **`GET /api/leaves/me`**（同样 **`credentials: 'include'`**），验证 Cookie 访问受保护接口。
- **`static/theme.css`**：最小样式。

## 验收

1. 浏览器打开 **`http://<host>:<port>/oa-demo/web/login.html`**（勿用 `file://`，否则无同源 Cookie）。  
2. DevTools → **Network**：登录响应头含 **`Set-Cookie`**；后续 **`/api/leaves/me`** 请求头含 **`Cookie: JSESSIONID=…`**。  
3. 见 **`oa-demo/README.md`** → **「W20 · Day134」**。
