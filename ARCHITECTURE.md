# 架构设计文档

本文档说明 CatVodSpider 项目的架构设计和模式。

---

## 📋 目录

- [架构概览](#架构概览)
- [设计原则](#设计原则)
- [接口契约](#接口契约)
- [依赖注入](#依赖注入)
- [扩展性](#扩展性)

---

## 架构概览

CatVodSpider 采用分层架构和接口抽象设计，遵循 SOLID 原则。

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                     │
│                  (Android UI / TVBox)                    │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                   API Contract Layer                     │
│             (ISpider, IHttpClient, IStorage)            │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                  Implementation Layer                    │
│         (Spider, OkHttp, SecureStorage, etc.)           │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    Infrastructure                        │
│      (Android SDK, OkHttp, Gson, Security-Crypto)       │
└─────────────────────────────────────────────────────────┘
```

### 核心层次

1. **Application Layer**（应用层）
   - UI 组件
   - Activity/Fragment
   - ViewModel

2. **API Contract Layer**（契约层）
   - 接口定义
   - 抽象契约
   - 无具体实现

3. **Implementation Layer**（实现层）
   - 具体业务逻辑
   - 爬虫实现
   - 工具类

4. **Infrastructure**（基础设施）
   - 第三方库
   - Android SDK
   - 系统服务

---

## 设计原则

CatVodSpider 遵循 SOLID 设计原则：

### 1. 单一职责原则（SRP）

每个类只负责一个职责。

```java
✅ 好的设计
class JsonValidator {
    // 只负责 JSON 验证
}

class SecureStorage {
    // 只负责加密存储
}

❌ 避免
class Utils {
    // 包含各种不相关的方法（JSON、加密、文件、网络...）
}
```

### 2. 开闭原则（OCP）

对扩展开放，对修改关闭。

```java
// 接口定义（不变）
interface ISpider {
    String searchContent(String keyword, boolean quick);
}

// 新的爬虫实现（扩展）
class NewSpider implements ISpider {
    @Override
    public String searchContent(String keyword, boolean quick) {
        // 新实现
    }
}
```

### 3. 里氏替换原则（LSP）

子类可以替换父类。

```java
ISpider spider = new MySpider();  // 或 new AnotherSpider()
String result = spider.searchContent("test", false);
// 无论哪个实现，调用方式相同
```

### 4. 接口隔离原则（ISP）

客户端不应依赖不使用的接口。

```java
✅ 好的设计
interface IHttpClient {
    String get(String url);
    HttpResponse post(String url, String json);
}

interface IStorage {
    void putString(String key, String value);
    String getString(String key, String defaultValue);
}

❌ 避免
interface IEverything {
    String get(String url);
    void putString(String key, String value);
    String encrypt(String data);
    // 太多不相关的方法
}
```

### 5. 依赖倒置原则（DIP）

依赖抽象而非具体实现。

```java
✅ 好的设计
class MySpider {
    private final IHttpClient httpClient;  // 依赖接口

    public MySpider(IHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}

❌ 避免
class MySpider {
    private final OkHttp okHttp = new OkHttp();  // 直接依赖具体类
}
```

---

## 接口契约

### ISpider - 爬虫接口

定义所有爬虫的标准方法。

**位置**：`com.github.catvod.api.contract.ISpider`

**方法**：
- `init(Context, String)` - 初始化
- `homeContent(boolean)` - 首页内容
- `categoryContent(...)` - 分类内容
- `detailContent(List)` - 详情内容
- `searchContent(String, boolean)` - 搜索
- `playerContent(...)` - 播放地址
- `destroy()` - 销毁资源

**向后兼容**：
- 现有的 `Spider` 类自动实现了这个接口
- 旧代码无需修改即可工作

### IHttpClient - HTTP 客户端接口

定义网络请求的标准方法。

**位置**：`com.github.catvod.api.contract.IHttpClient`

**方法**：
- `get(String url)` - GET 请求
- `post(String url, Map params)` - POST 表单
- `post(String url, String json)` - POST JSON

**优势**：
- 可以切换不同的 HTTP 库（OkHttp、Retrofit 等）
- 单元测试时可以注入 MockHttpClient
- 便于添加拦截器、重试逻辑

### IStorage - 存储接口

定义数据存储的标准方法。

**位置**：`com.github.catvod.api.contract.IStorage`

**方法**：
- `putString(String key, String value)` - 保存字符串
- `getString(String key, String defaultValue)` - 读取字符串
- `putInt/getInt` - 整数
- `putBoolean/getBoolean` - 布尔值
- `clear()` - 清空

**实现**：
- `SecureStorage` - 加密存储实现
- `PreferenceStorage` - SharedPreferences 实现（可选）

---

## 依赖注入

### 手动依赖注入

当前项目使用手动依赖注入，简单直接：

```java
// 创建依赖
IHttpClient httpClient = new OkHttpClientAdapter();
IStorage storage = new SecureStorageAdapter(context);

// 注入到爬虫
MySpider spider = new MySpider(httpClient, storage);
```

### 单元测试中的依赖注入

```java
@Test
public void testSearch() {
    // 创建 Mock 依赖
    IHttpClient mockHttpClient = mock(IHttpClient.class);
    when(mockHttpClient.get(anyString())).thenReturn("{\"result\":[]}");

    IStorage mockStorage = mock(IStorage.class);

    // 注入 Mock
    MySpider spider = new MySpider(mockHttpClient, mockStorage);

    // 测试
    String result = spider.searchContent("test", false);

    // 验证
    verify(mockHttpClient).get(contains("test"));
    assertNotNull(result);
}
```

### 工厂模式（可选）

对于复杂的依赖创建，可以使用工厂模式：

```java
public class SpiderFactory {
    public static ISpider createSpider(String type, Context context) {
        IHttpClient httpClient = new OkHttpClientAdapter();
        IStorage storage = new SecureStorageAdapter(context);

        switch (type) {
            case "douban":
                return new DoubanSpider(httpClient, storage);
            case "bilibili":
                return new BilibiliSpider(httpClient, storage);
            default:
                throw new IllegalArgumentException("Unknown spider type: " + type);
        }
    }
}
```

---

## 扩展性

### 添加新的爬虫

1. 实现 `ISpider` 接口：

```java
public class MySpider implements ISpider {

    private final IHttpClient httpClient;
    private final IStorage storage;

    public MySpider(IHttpClient httpClient, IStorage storage) {
        this.httpClient = httpClient;
        this.storage = storage;
    }

    @Override
    public void init(Context context, String extend) {
        // 初始化逻辑
    }

    @Override
    public String searchContent(String keyword, boolean quick) {
        // 使用 httpClient 发送请求
        String response = httpClient.get("https://api.example.com/search?q=" + keyword);
        return response;
    }

    // 实现其他方法...
}
```

2. 注册爬虫（在配置文件中）：

```json
{
  "sites": [
    {
      "key": "my_spider",
      "name": "我的爬虫",
      "api": "com.example.MySpider",
      "jar": "./spiders.jar"
    }
  ]
}
```

### 使用 OkHttpClientAdapter

项目已提供 `OkHttpClientAdapter` 实现 IHttpClient 接口：

```java
// 方式1：单例模式（推荐）
IHttpClient httpClient = OkHttpClientAdapter.getInstance();
String response = httpClient.get("https://api.example.com/list");

// 方式2：依赖注入
public class MySpider extends Spider {
    private final IHttpClient httpClient;

    public MySpider(IHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String searchContent(String keyword, boolean quick) {
        String response = httpClient.get("https://api.example.com/search?q=" + keyword);
        return parseResponse(response);
    }
}

// 创建爬虫实例
IHttpClient httpClient = OkHttpClientAdapter.getInstance();
MySpider spider = new MySpider(httpClient);
```

### 切换 HTTP 实现

如果想从 OkHttp 切换到其他 HTTP 库：

1. 创建适配器：

```java
public class RetrofitClientAdapter implements IHttpClient {

    private final Retrofit retrofit;

    public RetrofitClientAdapter() {
        this.retrofit = new Retrofit.Builder()
                .baseUrl("https://api.example.com")
                .build();
    }

    @Override
    public String get(String url) {
        // 使用 Retrofit 实现
    }

    @Override
    public HttpResponse post(String url, Map<String, String> params) {
        // 使用 Retrofit 实现
    }
}
```

2. 替换依赖：

```java
IHttpClient httpClient = new RetrofitClientAdapter();  // 改这一行
MySpider spider = new MySpider(httpClient, storage);
```

### 添加新的存储实现

例如，添加 Room 数据库存储：

```java
public class RoomStorageAdapter implements IStorage {

    private final AppDatabase database;

    public RoomStorageAdapter(Context context) {
        this.database = Room.databaseBuilder(
                context, AppDatabase.class, "storage.db"
        ).build();
    }

    @Override
    public void putString(String key, String value) {
        database.storageDao().insert(new StorageEntity(key, value));
    }

    @Override
    public String getString(String key, String defaultValue) {
        StorageEntity entity = database.storageDao().get(key);
        return entity != null ? entity.value : defaultValue;
    }

    // 实现其他方法...
}
```

---

## 架构演进

### 当前架构（v2.0）

- ✅ 接口抽象层
- ✅ 手动依赖注入
- ✅ 模块化设计
- ✅ 向后兼容

### 未来可能的改进

1. **依赖注入框架**
   - 使用 Dagger/Hilt 自动管理依赖
   - 减少手动创建对象的代码

2. **Repository 模式**
   - 添加 Repository 层统一数据访问
   - 支持多数据源（网络、数据库、缓存）

3. **Clean Architecture**
   - 进一步分层（Domain、Data、Presentation）
   - 完全独立的业务逻辑层

4. **响应式编程**
   - 使用 RxJava/Flow 处理异步操作
   - 更好的事件流管理

---

## 最佳实践

### 1. 依赖接口而非实现

```java
✅ 推荐
private final IHttpClient httpClient;

❌ 避免
private final OkHttp okHttp;
```

### 2. 通过构造函数注入依赖

```java
✅ 推荐
public MySpider(IHttpClient httpClient) {
    this.httpClient = httpClient;
}

❌ 避免
public MySpider() {
    this.httpClient = new OkHttp();  // 硬编码依赖
}
```

### 3. 使用工厂创建复杂对象

```java
✅ 推荐
ISpider spider = SpiderFactory.create("douban", context);

❌ 避免
IHttpClient httpClient = new OkHttp();
IStorage storage = new SecureStorage();
storage.init(context);
ISpider spider = new DoubanSpider(httpClient, storage);
```

### 4. 接口保持简洁

```java
✅ 推荐
interface IHttpClient {
    String get(String url);
    HttpResponse post(String url, String json);
}

❌ 避免
interface IHttpClient {
    String get(...);
    String post(...);
    void setProxy(...);
    void setCertificate(...);
    void enableLogging(...);
    // 太多方法
}
```

---

## 参考资源

- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)

---

**版本**: 2.0.0
**最后更新**: 2026-02-05
