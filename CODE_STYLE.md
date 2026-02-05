# CatVodSpider 代码风格指南

本文档定义了 CatVodSpider 项目的代码风格规范，确保代码一致性和可读性。

---

## 📋 目录

- [命名规范](#命名规范)
- [注释规范](#注释规范)
- [格式规范](#格式规范)
- [最佳实践](#最佳实践)

---

## 命名规范

### 类名

- 使用 **UpperCamelCase** 驼峰命名
- 名词或名词短语
- 清晰表达类的职责

```java
✅ 好的命名
public class JsonValidator { }
public class SecureStorage { }
public class OkHttpClient { }

❌ 避免的命名
public class validator { }        // 首字母小写
public class JSV { }              // 过度缩写
public class MyClass { }          // 无意义的名称
```

### 方法名

- 使用 **lowerCamelCase** 小驼峰命名
- 动词或动词短语
- 清晰表达方法的行为

```java
✅ 好的命名
public String validateJson(String json) { }
public void saveCookie(String cookie) { }
public boolean isValidPath(String path) { }

❌ 避免的命名
public String Validate(String json) { }     // 首字母大写
public void save(String cookie) { }         // 不够具体
public boolean valid(String path) { }       // 不是动词
```

### 变量名

- 使用 **lowerCamelCase** 小驼峰命名
- 名词或名词短语
- 避免单字母变量（除循环变量）

```java
✅ 好的命名
String userName;
int maxRetryCount;
List<File> fileList;

// 循环变量可以使用单字母
for (int i = 0; i < count; i++) { }

❌ 避免的命名
String s;                  // 不清晰
int n1, n2;               // 无意义
String UserName;          // 首字母大写
```

### 常量名

- 使用 **UPPER_SNAKE_CASE** 全大写下划线分隔
- 必须用 `static final` 修饰

```java
✅ 好的命名
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_ENCODING = "UTF-8";
private static final long TIMEOUT = 15000;

❌ 避免的命名
public static final int maxRetryCount = 3;    // 不是全大写
public static final String encoding = "UTF-8"; // 不是全大写
```

### 包名

- 全部小写
- 使用反向域名
- 避免下划线

```java
✅ 好的命名
package com.github.catvod.spider;
package com.github.catvod.utils;

❌ 避免的命名
package com.github.catvod.Spider;      // 包含大写
package com.github.catvod.my_utils;    // 包含下划线
```

---

## 注释规范

### JavaDoc 注释

所有 **public** 类、方法、字段必须有 JavaDoc 注释。

#### 类注释

```java
/**
 * JSON 验证工具类
 * <p>
 * 提供 JSON 格式验证和安全解析功能，防止 JSON 注入和 DoS 攻击。
 * </p>
 * <p>
 * <b>功能：</b>
 * <ul>
 *   <li>JSON 格式验证</li>
 *   <li>大小限制检查（10MB）</li>
 *   <li>深度限制检查（20层）</li>
 *   <li>安全字段提取</li>
 * </ul>
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * JsonObject root = JsonValidator.validateResponse(json, "object");
 * String title = JsonValidator.safeGetString(root, "title", "默认标题");
 * </pre>
 *
 * @author CatVod Team
 * @since 1.0.0
 * @see com.google.gson.JsonObject
 */
public class JsonValidator {
    // ...
}
```

#### 方法注释

```java
/**
 * 验证并解析 JSON 响应
 * <p>
 * 检查 JSON 格式、大小和深度，防止注入攻击和 DoS。
 * </p>
 *
 * @param json JSON 字符串
 * @param expectedType 期望的根类型（"object" 或 "array"）
 * @return 解析后的 JsonObject，验证失败返回空对象
 * @throws ValidationException 如果 JSON 格式无效
 * @throws IllegalArgumentException 如果 expectedType 无效
 */
public static JsonObject validateResponse(String json, String expectedType)
        throws ValidationException {
    // ...
}
```

#### 字段注释

```java
/**
 * 最大 JSON 大小限制（10MB）
 * <p>
 * 防止超大 JSON 导致内存溢出。
 * </p>
 */
private static final int MAX_JSON_SIZE = 10 * 1024 * 1024;
```

### 行内注释

- 使用 `//` 单行注释
- 注释应该解释 **为什么**，而非 **是什么**
- 注释前空一行

```java
✅ 好的注释
// 使用 SecureRandom 而非 Math.random()，因为密码学场景需要不可预测的随机数
SecureRandom random = new SecureRandom();

❌ 避免的注释
// 创建随机数生成器
SecureRandom random = new SecureRandom();  // 这只是重复代码
```

### TODO 注释

```java
// TODO: 从配置文件读取证书固定配置
// TODO(username): 优化大文件下载性能
// FIXME: 修复空指针异常（issue #123）
```

---

## 格式规范

### 缩进

- 使用 **4 个空格**，不使用 Tab
- 连续行缩进 **8 个空格**

```java
✅ 正确缩进
public void longMethodName(String param1, String param2,
        String param3, String param4) {
    if (condition1
            && condition2) {
        // ...
    }
}
```

### 大括号

- 使用 **K&R 风格**（左括号不换行）
- 即使单行代码也使用大括号

```java
✅ 推荐风格
if (condition) {
    doSomething();
}

❌ 避免
if (condition)
{  // 左括号换行
    doSomething();
}

if (condition) doSomething();  // 单行省略大括号（不推荐）
```

### 空行

- 类成员之间空一行
- 方法之间空一行
- 逻辑块之间空一行

```java
public class Example {

    private int field1;
    private String field2;  // 字段间空一行

    public void method1() {
        // 逻辑块1
        int a = 1;

        // 逻辑块2（空一行分隔）
        int b = 2;
    }

    public void method2() {  // 方法间空一行
        // ...
    }
}
```

### 行长度

- 最大 **120 字符**
- 超过时合理换行

```java
✅ 合理换行
String message = "这是一段很长的消息，"
        + "需要换行以保持代码可读性";

HttpResponse response = httpClient
        .newCall(request)
        .execute();
```

### Import 语句

- 不使用通配符 `*`
- 按字母顺序排序
- 分组：标准库 → 第三方库 → 项目内部

```java
✅ 推荐
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.github.catvod.utils.Util;

❌ 避免
import java.util.*;  // 不使用通配符
```

---

## 最佳实践

### 异常处理

```java
✅ 推荐
try {
    riskyOperation();
} catch (SpecificException e) {
    Logger.e("操作失败: " + context, e);
    // 处理异常或重新抛出
}

❌ 避免
try {
    riskyOperation();
} catch (Exception e) {
    // 空 catch 块（静默吞掉异常）
}
```

### 资源管理

```java
✅ 推荐：使用 try-with-resources
try (FileInputStream fis = new FileInputStream(file)) {
    // 自动关闭资源
}

❌ 避免：手动关闭
FileInputStream fis = new FileInputStream(file);
try {
    // ...
} finally {
    fis.close();  // 容易遗漏
}
```

### Null 检查

```java
✅ 推荐
if (str == null || str.isEmpty()) {
    return defaultValue;
}

// 或使用 TextUtils
if (TextUtils.isEmpty(str)) {
    return defaultValue;
}

❌ 避免
if (str.length() == 0) {  // 可能 NullPointerException
    return defaultValue;
}
```

### 常量定义

```java
✅ 推荐
public class Constants {
    public static final int MAX_RETRY = 3;
    public static final String DEFAULT_ENCODING = "UTF-8";

    private Constants() {
        // 防止实例化
    }
}

❌ 避免
public class MyClass {
    public void method() {
        int maxRetry = 3;  // 魔法数字
        String encoding = "UTF-8";  // 硬编码字符串
    }
}
```

---

## 工具配置

### IntelliJ IDEA / Android Studio

1. 导入代码风格配置
   - `File` → `Settings` → `Editor` → `Code Style`
   - 导入 `.editorconfig` 文件

2. 启用自动格式化
   - `Ctrl+Alt+L` (Windows/Linux)
   - `Cmd+Option+L` (Mac)

3. 配置保存时自动格式化
   - `File` → `Settings` → `Tools` → `Actions on Save`
   - 勾选 `Reformat code`

### 命令行格式化

```bash
# 使用 google-java-format
java -jar google-java-format.jar --replace src/**/*.java
```

---

## 检查清单

提交代码前，请确保：

- [ ] 所有 public 类和方法都有 JavaDoc
- [ ] 命名遵循规范（类名 UpperCamelCase，方法名 lowerCamelCase）
- [ ] 使用 4 空格缩进，无 Tab 字符
- [ ] 行长度不超过 120 字符
- [ ] 无空 catch 块
- [ ] 使用 try-with-resources 管理资源
- [ ] 无魔法数字和硬编码字符串
- [ ] Import 语句无通配符

---

**最后更新**: 2026-02-05
**版本**: 1.0.0
