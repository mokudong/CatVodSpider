# 贡献指南

感谢你对 CatVodSpider 项目的关注！本文档将帮助你了解如何为项目做出贡献。

---

## 📋 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发流程](#开发流程)
- [代码规范](#代码规范)
- [提交 Pull Request](#提交-pull-request)
- [报告问题](#报告问题)

---

## 行为准则

我们承诺为所有人提供一个友好、安全和包容的环境。参与本项目时，请：

- 尊重不同观点和经验
- 接受建设性批评
- 专注于对社区最有利的事情
- 对其他社区成员表示同理心

---

## 如何贡献

你可以通过以下方式为项目做出贡献：

### 1. 报告 Bug

如果发现 Bug，请[创建 Issue](https://github.com/mokudong/CatVodSpider/issues/new)，并包含：

- 问题的清晰描述
- 重现步骤
- 预期行为和实际行为
- 环境信息（Android 版本、设备型号）
- 相关日志或截图

### 2. 提出新功能

如果有新功能建议，请：

- 先检查是否已有类似 Issue
- 创建 Issue 说明功能的用途和价值
- 提供详细的使用场景
- 如果可能，提供设计草图或伪代码

### 3. 贡献代码

欢迎提交代码修复或新功能！请遵循以下流程。

### 4. 改进文档

文档改进同样重要：

- 修正错别字和语法错误
- 补充缺失的文档
- 改进示例代码
- 翻译文档

---

## 开发流程

### 环境准备

1. **Fork 仓库**

```bash
# 点击 GitHub 页面右上角的 Fork 按钮
```

2. **克隆到本地**

```bash
git clone https://github.com/your-username/CatVodSpider.git
cd CatVodSpider
```

3. **添加上游仓库**

```bash
git remote add upstream https://github.com/mokudong/CatVodSpider.git
```

4. **安装依赖**

- Android Studio 2024.x+
- JDK 17
- Android SDK (API 36)

### 开发步骤

1. **创建分支**

```bash
git checkout -b feature/your-feature-name
# 或
git checkout -b fix/issue-123
```

分支命名规范：
- `feature/xxx` - 新功能
- `fix/xxx` - Bug 修复
- `docs/xxx` - 文档更新
- `refactor/xxx` - 代码重构
- `test/xxx` - 测试相关

2. **编写代码**

遵循[代码风格指南](./CODE_STYLE.md)：
- 添加必要的注释
- 编写单元测试
- 确保代码通过 Lint 检查

3. **提交代码**

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```bash
git add .
git commit -m "feat: 添加 XXX 功能"
```

提交信息格式：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type（必需）**：
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式（不影响代码运行）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试
- `chore`: 构建过程或辅助工具变动
- `security`: 安全修复

**示例**：

```bash
feat(spider): 添加豆瓣电影爬虫

- 实现豆瓣电影搜索
- 支持详情页解析
- 添加单元测试

Closes #123
```

4. **同步上游更新**

```bash
git fetch upstream
git rebase upstream/main
```

5. **推送到 Fork**

```bash
git push origin feature/your-feature-name
```

---

## 代码规范

### 必须遵守

1. **代码风格**：遵循 [CODE_STYLE.md](./CODE_STYLE.md)
2. **安全规范**：参考 [SECURITY_AUDIT_REPORT.md](./SECURITY_AUDIT_REPORT.md)
3. **测试覆盖**：新功能必须包含单元测试
4. **文档更新**：公共 API 必须有 JavaDoc

### 检查清单

提交前请确保：

- [ ] 代码遵循项目风格规范
- [ ] 所有测试通过
- [ ] 添加了必要的注释和文档
- [ ] 没有引入新的安全漏洞
- [ ] 提交信息清晰明确
- [ ] 代码通过 Lint 检查

---

## 提交 Pull Request

### PR 标题

使用清晰的标题描述你的更改：

```
feat: 添加 XXX 功能
fix: 修复 XXX 问题
docs: 更新 XXX 文档
```

### PR 描述模板

```markdown
## 变更类型

- [ ] 新功能
- [ ] Bug 修复
- [ ] 文档更新
- [ ] 代码重构
- [ ] 性能优化

## 变更说明

简要描述你的更改和原因。

## 相关 Issue

Closes #123

## 测试

描述如何测试这些更改。

- [ ] 单元测试
- [ ] 手动测试
- [ ] 集成测试

## 截图（如适用）

如果是 UI 变更，请提供前后对比截图。

## 检查清单

- [ ] 代码遵循项目规范
- [ ] 已添加必要的文档
- [ ] 所有测试通过
- [ ] 没有引入新的安全问题
```

### 审查流程

1. 提交 PR 后，CI 会自动运行测试
2. 维护者会审查你的代码
3. 如有需要修改，请及时响应反馈
4. 审查通过后会合并到主分支

---

## 报告问题

### 安全漏洞

**请勿公开报告安全漏洞！**

如果发现安全问题，请通过以下方式私下报告：

- 发送邮件至：security@example.com
- 在 GitHub 使用 [Security Advisory](https://github.com/mokudong/CatVodSpider/security/advisories/new)

我们会在 48 小时内响应，并在修复后公开致谢。

### 一般问题

对于非安全问题，请[创建 Issue](https://github.com/mokudong/CatVodSpider/issues/new)。

---

## 认可贡献者

所有贡献者都会被记录在项目中：

- 重要贡献会在 Release Notes 中提及
- 贡献者列表会定期更新
- 代码提交会显示 Co-Authored-By 信息

---

## 许可证

通过向本项目提交代码，你同意你的贡献将在 [GPL-3.0](./LICENSE) 许可证下发布。

---

## 联系方式

如有任何问题，欢迎通过以下方式联系：

- GitHub Issues: https://github.com/mokudong/CatVodSpider/issues
- GitHub Discussions: https://github.com/mokudong/CatVodSpider/discussions

---

**感谢你的贡献！** 🎉
