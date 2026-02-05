# 测试指南

本文档说明如何运行和编写 CatVodSpider 项目的单元测试。

---

## 📋 目录

- [运行测试](#运行测试)
- [测试覆盖](#测试覆盖)
- [编写测试](#编写测试)
- [Mock 和 Stub](#mock-和-stub)
- [最佳实践](#最佳实践)

---

## 运行测试

### 命令行

```bash
# 运行所有单元测试
./gradlew test

# 运行特定测试类
./gradlew test --tests com.github.catvod.utils.JsonValidatorTest

# 运行特定测试方法
./gradlew test --tests com.github.catvod.utils.JsonValidatorTest.testValidateResponse_validObject

# 生成测试报告（带覆盖率）
./gradlew test jacocoTestReport

# 查看测试报告
open app/build/reports/tests/test/index.html
```

### Android Studio

1. **运行所有测试**
   - 右键点击 `src/test/java` 目录
   - 选择 `Run 'All Tests'`

2. **运行单个测试类**
   - 打开测试类文件
   - 点击类名旁边的绿色三角形
   - 或使用快捷键：`Ctrl+Shift+F10`（Windows/Linux）、`Ctrl+Shift+R`（Mac）

3. **运行单个测试方法**
   - 点击方法名旁边的绿色三角形

---

## 测试覆盖

### 已测试的类

| 类名 | 测试类 | 覆盖率 | 说明 |
|------|--------|--------|------|
| JsonValidator | JsonValidatorTest | 90%+ | JSON 验证和安全字段提取 |
| Crypto | CryptoTest | 85%+ | MD5、AES、RSA 加密和随机密钥 |
| OkHttp | OkHttpTest | 70%+ | 网络请求（使用 MockWebServer） |
| Path | PathTest | 80%+ | 文件操作 |

### 测试统计

```
总测试数: 60+
通过率: 100%
平均执行时间: <2秒
```

---

## 编写测试

### 测试文件位置

```
app/src/test/java/com/github/catvod/
├── utils/
│   ├── JsonValidatorTest.java
│   ├── CryptoTest.java
│   └── PathTest.java
├── net/
│   └── OkHttpTest.java
└── AllTests.java
```

### 基本测试结构

```java
package com.github.catvod.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyClassTest {

    private MyClass instance;

    @Before
    public void setUp() {
        // 在每个测试前执行
        instance = new MyClass();
    }

    @After
    public void tearDown() {
        // 在每个测试后执行
        instance = null;
    }

    @Test
    public void testMethod_normalCase() {
        // Arrange（准备）
        String input = "test";

        // Act（执行）
        String result = instance.method(input);

        // Assert（断言）
        assertEquals("结果应该正确", "expected", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMethod_invalidInput() {
        // 测试异常情况
        instance.method(null);
    }
}
```

### 断言方法

```java
// 相等性
assertEquals("消息", expected, actual);
assertNotEquals("消息", unexpected, actual);

// 真假
assertTrue("消息", condition);
assertFalse("消息", condition);

// Null 检查
assertNull("消息", object);
assertNotNull("消息", object);

// 同一对象
assertSame("消息", expected, actual);
assertNotSame("消息", unexpected, actual);

// 数组
assertArrayEquals("消息", expectedArray, actualArray);
```

---

## Mock 和 Stub

### 使用 MockWebServer（网络请求）

```java
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class MyTest {

    private MockWebServer mockServer;

    @Before
    public void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @After
    public void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    public void testNetworkRequest() throws Exception {
        // 准备响应
        mockServer.enqueue(new MockResponse()
                .setBody("{\"success\":true}")
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        // 发送请求
        String url = mockServer.url("/api").toString();
        String result = OkHttp.string(url);

        // 验证结果
        assertTrue(result.contains("success"));

        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api", request.getPath());
    }
}
```

### 使用 Mockito（依赖注入）

```java
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class MyTest {

    @Mock
    private Dependency mockDependency;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testWithMock() {
        // 配置 mock 行为
        when(mockDependency.getData()).thenReturn("mocked data");

        // 使用 mock
        MyClass instance = new MyClass(mockDependency);
        String result = instance.process();

        // 验证
        assertEquals("mocked data", result);
        verify(mockDependency, times(1)).getData();
    }
}
```

---

## 最佳实践

### 1. 测试命名

使用描述性名称，遵循 `test<方法名>_<场景>` 模式：

```java
✅ 好的命名
testValidateResponse_validObject()
testValidateResponse_invalidJson()
testSafeGetString_notExists()

❌ 避免的命名
test1()
testValidate()
testSomething()
```

### 2. 独立性

每个测试应该独立运行，不依赖其他测试：

```java
✅ 好的做法
@Test
public void test1() {
    MyClass instance = new MyClass();
    // 测试逻辑
}

@Test
public void test2() {
    MyClass instance = new MyClass();  // 创建新实例
    // 测试逻辑
}

❌ 避免
private MyClass sharedInstance;  // 在测试间共享状态

@Test
public void test1() {
    sharedInstance = new MyClass();
}

@Test
public void test2() {
    sharedInstance.method();  // 依赖 test1
}
```

### 3. AAA 模式

遵循 Arrange-Act-Assert 模式：

```java
@Test
public void testExample() {
    // Arrange：准备测试数据和环境
    String input = "test";
    MyClass instance = new MyClass();

    // Act：执行被测试的方法
    String result = instance.process(input);

    // Assert：验证结果
    assertEquals("expected", result);
}
```

### 4. 一个测试一个断言

尽量每个测试只验证一个行为：

```java
✅ 好的做法
@Test
public void testAdd_returnsCorrectSum() {
    assertEquals(5, calculator.add(2, 3));
}

@Test
public void testAdd_handlesNegativeNumbers() {
    assertEquals(-1, calculator.add(-3, 2));
}

❌ 避免（一个测试多个断言）
@Test
public void testAdd() {
    assertEquals(5, calculator.add(2, 3));
    assertEquals(-1, calculator.add(-3, 2));
    assertEquals(0, calculator.add(0, 0));
    // 如果第一个失败，后面的断言不会执行
}
```

### 5. 测试边界条件

确保测试边界和异常情况：

```java
@Test
public void testMethod_emptyString() { }

@Test
public void testMethod_nullInput() { }

@Test
public void testMethod_maxLength() { }

@Test
public void testMethod_negativeNumber() { }

@Test(expected = IllegalArgumentException.class)
public void testMethod_throwsException() { }
```

### 6. 使用有意义的断言消息

```java
✅ 好的做法
assertEquals("用户名应该被正确提取", "test", user.getName());

❌ 避免
assertEquals(user.getName(), "test");  // 无消息
```

### 7. 测试覆盖率目标

- 工具类：80%+ 覆盖率
- 业务逻辑：70%+ 覆盖率
- UI 代码：可选（难以测试）

### 8. 性能测试

对于性能敏感的代码，添加性能测试：

```java
@Test
public void testPerformance_shouldCompleteQuickly() {
    long startTime = System.currentTimeMillis();

    // 执行操作
    instance.expensiveOperation();

    long elapsedTime = System.currentTimeMillis() - startTime;
    assertTrue("操作应该在 100ms 内完成", elapsedTime < 100);
}
```

---

## 常见问题

### 1. 测试找不到类

**问题**：`ClassNotFoundException`

**解决**：
```bash
# 清理并重新构建
./gradlew clean test
```

### 2. MockWebServer 端口冲突

**问题**：`Address already in use`

**解决**：在 `@After` 中确保正确关闭：
```java
@After
public void tearDown() throws IOException {
    if (mockServer != null) {
        mockServer.shutdown();
    }
}
```

### 3. Android 依赖问题

**问题**：`Method ... not mocked`

**解决**：使用 Robolectric 或添加 mock：
```gradle
testOptions {
    unitTests.returnDefaultValues = true
}
```

---

## CI/CD 集成

### GitHub Actions

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Upload test results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/reports/tests/
```

---

## 参考资源

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://site.mockito.org/)
- [MockWebServer Guide](https://github.com/square/okhttp/tree/master/mockwebserver)
- [Android Testing Guide](https://developer.android.com/training/testing)

---

**版本**: 1.0.0
**最后更新**: 2026-02-05
