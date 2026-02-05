# 配置文件使用说明

## 安全配置指南

### ⚠️ 重要提示

**请勿在代码仓库中提交包含真实凭证的配置文件！**

本目录下的 `config.json` 文件包含敏感凭证信息，已被 `.gitignore` 忽略。

---

## 快速开始

### 1. 复制模板文件

```bash
cp config.json.example config.json
```

### 2. 填写真实凭证

编辑 `config.json`，替换以下占位符：

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `{{IPTV_API_URL}}` | IPTV API 地址 | `http://example.com:25461/player_api.php?username=xxx&password=xxx` |
| `{{IPTV_EPG_URL}}` | EPG 节目单地址 | `http://example.com:25461/xmltv.php?username=xxx&password=xxx` |
| `{{HOTEL_A_URL}}` | 酒店 A 地址 | `https://hotel-a.example.com:4433` |
| `{{HOTEL_B_URL}}` | 酒店 B 地址 | `http://hotel-b.example.com` |

### 3. 验证配置

确保 URL 格式正确，包含完整的协议、主机名和端口。

---

## 配置项说明

### XtreamCode (IPTV)

```json
{
  "name": "XtreamCode",
  "api": "csp_XtreamCode",
  "url": "http://your-server:port/player_api.php?username=YOUR_USERNAME&password=YOUR_PASSWORD",
  "epg": "http://your-server:port/xmltv.php?username=YOUR_USERNAME&password=YOUR_PASSWORD"
}
```

**必填字段**:
- `url`: IPTV API 地址，需包含用户名和密码参数
- `epg`: EPG 节目单地址

### MQiTV (酒店 IPTV)

```json
{
  "name": "MQiTV",
  "api": "csp_MQiTV",
  "ext": [
    {
      "name": "Hotel A",
      "url": "https://your-hotel-url:port"
    }
  ]
}
```

**必填字段**:
- `url`: 酒店 IPTV 服务器地址

---

## 安全最佳实践

### ✅ 推荐做法

1. **使用环境变量**
   ```java
   String apiUrl = System.getenv("IPTV_API_URL");
   ```

2. **使用远程配置服务器**
   - 将凭证存储在安全的配置服务器
   - 应用运行时动态获取配置

3. **实现用户自主配置**
   - 提供 UI 界面让用户输入凭证
   - 使用 EncryptedSharedPreferences 安全存储

4. **使用 OAuth2/JWT 令牌**
   - 避免使用永久密码
   - 使用短期访问令牌

### ❌ 避免做法

1. **不要在代码中硬编码凭证**
   ```java
   // ❌ 错误示例
   String password = "12345";
   ```

2. **不要将 config.json 提交到 Git**
   ```bash
   # ❌ 不要执行
   git add json/config.json
   ```

3. **不要在日志中输出凭证**
   ```java
   // ❌ 错误示例
   Logger.d("Password: " + password);
   ```

4. **不要在 APK 中包含真实凭证**
   - Release 版本应使用占位符
   - 真实凭证由用户输入

---

## 示例配置

### 开发环境

```json
{
  "url": "http://localhost:25461/player_api.php?username=test&password=test",
  "epg": "http://localhost:25461/xmltv.php?username=test&password=test"
}
```

### 生产环境

```json
{
  "url": "{{IPTV_API_URL}}",
  "epg": "{{IPTV_EPG_URL}}"
}
```

用户首次运行应用时，通过 UI 界面输入真实凭证。

---

## 常见问题

### Q: 为什么需要分离配置文件？

A: 防止敏感凭证泄露到代码仓库，保护用户账户安全。

### Q: 如果误提交了包含凭证的文件怎么办？

A: 立即执行以下操作：
```bash
# 1. 从 Git 历史中删除
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch json/config.json" \
  --prune-empty --tag-name-filter cat -- --all

# 2. 强制推送
git push origin --force --all

# 3. 轮换泄露的凭证
# 联系服务提供商更换密码
```

### Q: 如何验证配置文件格式？

A: 使用 JSON 验证工具：
```bash
cat config.json | jq .
```

---

## 相关文档

- [安全审查报告](../SECURITY_AUDIT_REPORT.md)
- [项目文档](../CLAUDE.md)
- [安全最佳实践](../SECURITY_AUDIT_REPORT.md#修复建议)

---

**最后更新**: 2026-02-05
