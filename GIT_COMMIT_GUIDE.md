# Git 提交指南

本文档指导如何将安全修复提交到代码仓库。

---

## 📋 修改清单

### 新增文件（8个）

```bash
# 安全工具类
app/src/main/java/com/github/catvod/utils/JsonValidator.java
app/src/main/java/com/github/catvod/utils/SecureStorage.java

# 配置文件
app/src/main/res/xml/network_security_config.xml
json/config.json.example
json/CONFIG_README.md

# 文档文件
SECURITY_AUDIT_REPORT.md
SECURITY_ISSUES_CHECKLIST.md
SECURITY_FIX_SUMMARY.md
GIT_COMMIT_GUIDE.md (本文件)
```

### 修改文件（7个）

```bash
app/build.gradle                                        # 添加 BuildConfig 和安全依赖
app/src/main/AndroidManifest.xml                        # 禁用明文流量
app/src/main/java/com/github/catvod/net/OkHttp.java    # SSL 验证修复
app/src/main/java/com/github/catvod/utils/Shell.java   # 命令注入修复
app/src/main/java/com/github/catvod/utils/Path.java    # 移除 chmod 777
.gitignore                                              # 忽略敏感配置
```

---

## 🚀 提交步骤

### 步骤 1: 查看修改

```bash
cd /e/ClaudeCode_Work/TVBox/CatVodSpider

# 查看所有修改
git status

# 查看详细差异
git diff
```

### 步骤 2: 分批提交（推荐）

建议分多个提交，方便审查和回滚：

#### 提交 1: SSL/TLS 验证修复

```bash
git add app/build.gradle
git add app/src/main/java/com/github/catvod/net/OkHttp.java

git commit -m "security: enable SSL/TLS certificate verification in production

- Add BuildConfig field DISABLE_SSL_VERIFICATION
- Only disable SSL verification in debug builds
- Production builds now enforce full certificate validation
- Add security warnings in logs

Fixes: CRITICAL vulnerability CWE-295
Impact: Prevents Man-in-the-Middle (MITM) attacks"
```

#### 提交 2: 移除硬编码凭证

```bash
git add json/config.json.example
git add json/CONFIG_README.md
git add .gitignore

# 注意：不要 add json/config.json（如果包含真实凭证）

git commit -m "security: remove hardcoded credentials from config

- Create config.json.example template with placeholders
- Add CONFIG_README.md with security best practices
- Update .gitignore to exclude json/config.json

Fixes: CRITICAL vulnerability CWE-798
Impact: Prevents credential leakage

BREAKING CHANGE: Users must copy config.json.example to config.json
and fill in their own credentials"
```

#### 提交 3: 禁用明文 HTTP 流量

```bash
git add app/src/main/AndroidManifest.xml
git add app/src/main/res/xml/network_security_config.xml

git commit -m "security: disable cleartext HTTP traffic

- Set usesCleartextTraffic=false in AndroidManifest
- Add network_security_config.xml for fine-grained control
- Only allow cleartext for localhost (debugging)
- Debug builds allow user-installed CA certificates (Charles Proxy)

Fixes: CRITICAL vulnerability CWE-319
Impact: Enforces HTTPS for all network traffic"
```

#### 提交 4: 修复 Shell 命令注入

```bash
git add app/src/main/java/com/github/catvod/utils/Shell.java
git add app/src/main/java/com/github/catvod/utils/Path.java

git commit -m "security: fix shell command injection vulnerability

- Rewrite Shell.java to use ProcessBuilder (auto-escapes arguments)
- Deprecate unsafe Shell.exec(String) method
- Remove chmod 777 from Path.java
- Use Java file permission APIs instead (equivalent to chmod 644)

Fixes: CRITICAL vulnerability CWE-78
Impact: Prevents arbitrary command execution

BREAKING CHANGE: Shell.exec(String) is now deprecated
Use Shell.exec(String...) instead:
  Before: Shell.exec(\"chmod 755 \" + file)
  After: Shell.exec(\"chmod\", \"755\", file.getAbsolutePath())"
```

#### 提交 5: 实现 JSON 验证

```bash
git add app/src/main/java/com/github/catvod/utils/JsonValidator.java

git commit -m "security: add JSON validation to prevent injection attacks

- Create JsonValidator utility class
- Implement size limit (10MB) and depth limit (20 levels)
- Add safe field extraction methods (null-safe)
- Validate API response structure

Fixes: CRITICAL vulnerability CWE-502
Impact: Prevents JSON injection and DoS attacks

Usage:
  JsonObject root = JsonValidator.validateResponse(json, \"object\");
  String title = JsonValidator.safeGetString(root, \"title\", \"default\");"
```

#### 提交 6: 实现加密存储

```bash
git add app/build.gradle  # (如果还没提交)
git add app/src/main/java/com/github/catvod/utils/SecureStorage.java

git commit -m "security: implement encrypted credential storage

- Add AndroidX Security Crypto dependency
- Create SecureStorage utility class
- Use AES256-GCM encryption (hardware-backed)
- Store master key in Android Keystore

Fixes: CRITICAL vulnerability CWE-311
Impact: Prevents session hijacking from plaintext storage

Usage:
  SecureStorage.init(context);
  SecureStorage.saveCookie(cookie);
  String cookie = SecureStorage.getCookie();"
```

#### 提交 7: 添加文档

```bash
git add SECURITY_AUDIT_REPORT.md
git add SECURITY_ISSUES_CHECKLIST.md
git add SECURITY_FIX_SUMMARY.md
git add GIT_COMMIT_GUIDE.md

git commit -m "docs: add security audit and fix documentation

- SECURITY_AUDIT_REPORT.md: Comprehensive security review
- SECURITY_ISSUES_CHECKLIST.md: Issue tracking checklist
- SECURITY_FIX_SUMMARY.md: Fix summary and migration guide
- GIT_COMMIT_GUIDE.md: Git commit instructions

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

### 步骤 3: 推送到远程

```bash
# 推送到远程仓库
git push origin main

# 或者推送到特性分支（推荐）
git checkout -b security-fixes
git push origin security-fixes

# 然后在 GitHub 创建 Pull Request
```

---

## 🔄 一键提交（替代方案）

如果希望一次性提交所有修改：

```bash
git add -A

git commit -m "security: fix 6 critical vulnerabilities

This commit addresses all critical security issues identified in the
security audit:

1. SSL/TLS Certificate Verification
   - Enable certificate validation in production builds
   - Only disable in debug builds
   CWE-295: Improper Certificate Validation

2. Hardcoded Credentials
   - Remove plaintext credentials from config.json
   - Create config.json.example template
   CWE-798: Use of Hard-coded Credentials

3. Cleartext HTTP Traffic
   - Disable usesCleartextTraffic
   - Add network_security_config.xml
   CWE-319: Cleartext Transmission of Sensitive Information

4. Shell Command Injection
   - Replace Runtime.exec() with ProcessBuilder
   - Remove chmod 777
   CWE-78: OS Command Injection

5. Unsafe JSON Deserialization
   - Add JsonValidator utility class
   - Implement size and depth limits
   CWE-502: Deserialization of Untrusted Data

6. Plaintext Credential Storage
   - Add SecureStorage utility class
   - Use AES256-GCM encryption
   CWE-311: Missing Encryption of Sensitive Data

BREAKING CHANGES:
- Shell.exec(String) is deprecated, use Shell.exec(String...)
- Users must create json/config.json from config.json.example
- Code using plaintext storage must migrate to SecureStorage

See SECURITY_FIX_SUMMARY.md for migration guide.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## 🧹 清理历史中的敏感数据

如果 `json/config.json` 之前已提交到 Git 历史：

### 方法 1: BFG Repo-Cleaner (推荐)

```bash
# 下载 BFG
wget https://repo1.maven.org/maven2/com/madgag/bfg/1.14.0/bfg-1.14.0.jar

# 删除包含 "password" 的文件
java -jar bfg-1.14.0.jar --delete-files config.json

# 清理
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# 强制推送
git push origin --force --all
```

### 方法 2: git filter-branch

```bash
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch json/config.json" \
  --prune-empty --tag-name-filter cat -- --all

git reflog expire --expire=now --all
git gc --prune=now --aggressive

git push origin --force --all
```

### ⚠️ 重要提醒

清理 Git 历史后，**必须轮换所有泄露的凭证**：

1. 联系 IPTV 服务提供商更换密码
2. 撤销所有访问令牌
3. 更新所有使用旧凭证的系统

---

## 📝 Pull Request 模板

如果使用 Pull Request 流程：

```markdown
## 安全漏洞修复

### 修复内容

本 PR 修复了安全审计中发现的 **6 个严重安全漏洞**：

- [x] SSL/TLS 证书验证禁用 (CWE-295)
- [x] 硬编码明文凭证 (CWE-798)
- [x] 允许明文 HTTP 流量 (CWE-319)
- [x] Shell 命令注入 (CWE-78)
- [x] 不安全的 JSON 反序列化 (CWE-502)
- [x] Cookie/Token 明文存储 (CWE-311)

### 影响

- **新增**: 3 个安全工具类 (JsonValidator, SecureStorage, Shell v2.0)
- **新增**: 1 个依赖 (AndroidX Security Crypto)
- **修改**: 7 个核心文件
- **破坏性变更**: 详见下方

### 破坏性变更

1. **Shell 命令调用方式变化**
   ```java
   // 旧代码（已弃用）
   Shell.exec("chmod 755 " + filename);

   // 新代码
   Shell.exec("chmod", "755", filename);
   ```

2. **配置文件使用方式变化**
   ```bash
   # 用户需要手动创建配置文件
   cp json/config.json.example json/config.json
   # 然后填写真实凭证
   ```

3. **凭证存储方式变化**
   ```java
   // 初始化（Application.onCreate）
   SecureStorage.init(context);

   // 使用加密存储替代明文文件
   SecureStorage.saveCookie(cookie);
   ```

### 测试

- [ ] 编译通过（调试版本）
- [ ] 编译通过（发布版本）
- [ ] 网络请求正常（HTTPS）
- [ ] JSON 解析正常
- [ ] 凭证加密存储正常
- [ ] 文件权限正确（644 而非 777）

### 文档

- [x] SECURITY_AUDIT_REPORT.md - 完整安全审查报告
- [x] SECURITY_FIX_SUMMARY.md - 修复总结和迁移指南
- [x] SECURITY_ISSUES_CHECKLIST.md - 问题跟踪清单
- [x] json/CONFIG_README.md - 配置文件使用说明

### Checklist

- [x] 代码遵循项目规范
- [x] 已添加必要的文档
- [x] 没有引入新的安全漏洞
- [x] 所有严重安全问题已修复
- [ ] 已在本地测试（需要 JVM 17 环境）
- [ ] 已由安全团队审查

### 相关 Issue

Closes #XX (安全审计 Issue)

---

**审查重点**:
1. SSL/TLS 验证逻辑是否正确
2. 敏感数据是否全部加密
3. Shell 命令是否安全
4. 破坏性变更是否可接受
```

---

## ✅ 提交前检查清单

- [ ] 所有修改已测试
- [ ] 没有遗留的 TODO 或 FIXME
- [ ] 没有调试代码（console.log, System.out.println）
- [ ] 没有注释掉的代码
- [ ] 提交信息清晰明确
- [ ] 没有提交敏感信息（密码、密钥、令牌）
- [ ] `.gitignore` 已更新
- [ ] 文档已更新

---

## 📞 联系方式

如有问题，请联系：
- 安全团队: security@example.com
- 项目负责人: [待填写]

---

**修复日期**: 2026-02-05
**修复版本**: v1.1.0 (建议)
