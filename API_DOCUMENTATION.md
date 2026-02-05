# CatVodSpider API 文档

本文档详细说明 CatVodSpider 爬虫框架的 API 接口。

---

## 📋 目录

- [Spider 基类](#spider-基类)
- [网络请求 API](#网络请求-api)
- [加密工具 API](#加密工具-api)
- [JSON 验证 API](#json-验证-api)
- [安全存储 API](#安全存储-api)
- [文件操作 API](#文件操作-api)

---

## Spider 基类

所有爬虫必须继承 `com.github.catvod.crawler.Spider` 类。

### 生命周期方法

#### init()

```java
/**
 * 初始化爬虫
 *
 * @param context Android Context
 * @param extend  扩展参数（来自配置文件的 ext 字段）
 */
public void init(Context context, String extend)
```

**使用示例**：

```java
@Override
public void init(Context context, String extend) {
    // 解析扩展参数
    JsonObject config = JsonParser.parseString(extend).getAsJsonObject();
    this.apiKey = config.get("apiKey").getAsString();
}
```

#### homeContent()

```java
/**
 * 获取首页内容（分类列表）
 *
 * @param filter 是否需要筛选条件
 * @return JSON 字符串
 */
public String homeContent(boolean filter)
```

**返回格式**：

```json
{
  "class": [
    {"type_id": "1", "type_name": "电影"},
    {"type_id": "2", "type_name": "电视剧"}
  ],
  "filters": {
    "1": [
      {
        "key": "area",
        "name": "地区",
        "value": [
          {"n": "全部", "v": ""},
          {"n": "大陆", "v": "大陆"}
        ]
      }
    ]
  }
}
```

#### categoryContent()

```java
/**
 * 获取分类内容（视频列表）
 *
 * @param tid    分类ID
 * @param pg     页码
 * @param filter 是否启用筛选
 * @param extend 筛选参数（key-value）
 * @return JSON 字符串
 */
public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend)
```

**返回格式**：

```json
{
  "page": 1,
  "pagecount": 100,
  "limit": 20,
  "total": 2000,
  "list": [
    {
      "vod_id": "123",
      "vod_name": "电影名称",
      "vod_pic": "https://example.com/poster.jpg",
      "vod_remarks": "HD"
    }
  ]
}
```

#### detailContent()

```java
/**
 * 获取视频详情
 *
 * @param ids 视频ID列表
 * @return JSON 字符串
 */
public String detailContent(List<String> ids)
```

**返回格式**：

```json
{
  "list": [
    {
      "vod_id": "123",
      "vod_name": "电影名称",
      "vod_pic": "https://example.com/poster.jpg",
      "vod_year": "2024",
      "vod_area": "中国",
      "vod_remarks": "HD",
      "vod_actor": "演员1,演员2",
      "vod_director": "导演",
      "vod_content": "剧情简介",
      "vod_play_from": "线路1$$$线路2",
      "vod_play_url": "第1集$https://url1#第2集$https://url2$$$第1集$https://url3"
    }
  ]
}
```

#### searchContent()

```java
/**
 * 搜索视频
 *
 * @param key   搜索关键词
 * @param quick 是否快速搜索
 * @return JSON 字符串
 */
public String searchContent(String key, boolean quick)
```

#### playerContent()

```java
/**
 * 获取播放地址
 *
 * @param flag     线路标识
 * @param id       播放ID
 * @param vipFlags VIP线路标识列表
 * @return JSON 字符串
 */
public String playerContent(String flag, String id, List<String> vipFlags)
```

**返回格式**：

```json
{
  "parse": 0,
  "url": "https://example.com/video.m3u8",
  "header": {
    "User-Agent": "Mozilla/5.0"
  }
}
```

#### destroy()

```java
/**
 * 销毁爬虫（释放资源）
 */
public void destroy()
```

---

## 网络请求 API

### OkHttp 工具类

位置：`com.github.catvod.net.OkHttp`

#### 发送 GET 请求

```java
/**
 * 发送 GET 请求并返回字符串
 *
 * @param url 请求URL
 * @return 响应内容
 */
public static String string(String url)

/**
 * 发送 GET 请求并返回字符串（带请求头）
 *
 * @param url    请求URL
 * @param header 请求头
 * @return 响应内容
 */
public static String string(String url, Map<String, String> header)
```

**使用示例**：

```java
// 简单GET请求
String html = OkHttp.string("https://api.example.com/list");

// 带请求头
Map<String, String> headers = new HashMap<>();
headers.put("User-Agent", "Mozilla/5.0");
String html = OkHttp.string("https://api.example.com/list", headers);
```

#### 发送 POST 请求

```java
/**
 * 发送 POST 请求
 *
 * @param url    请求URL
 * @param params POST参数
 * @return OkResult对象（包含状态码、响应体、响应头）
 */
public static OkResult post(String url, Map<String, String> params)

/**
 * 发送 POST 请求（JSON Body）
 *
 * @param url  请求URL
 * @param json JSON字符串
 * @return OkResult对象
 */
public static OkResult post(String url, String json)
```

**使用示例**：

```java
// POST 表单
Map<String, String> params = new HashMap<>();
params.put("username", "test");
params.put("password", "123456");
OkResult result = OkHttp.post("https://api.example.com/login", params);

// POST JSON
String json = "{\"username\":\"test\",\"password\":\"123456\"}";
OkResult result = OkHttp.post("https://api.example.com/login", json);

// 获取响应
int statusCode = result.getCode();
String body = result.getBody();
Map<String, List<String>> headers = result.getHeader();
```

#### 超时配置

```java
// 使用预定义超时常量
public static final long TIMEOUT_FAST = 5000;   // 5秒（健康检查）
public static final long TIMEOUT_SLOW = 30000;  // 30秒（大文件下载）
```

---

## 加密工具 API

位置：`com.github.catvod.utils.Crypto`

#### MD5 哈希

```java
/**
 * 计算MD5哈希值
 *
 * @param src 源字符串
 * @return 32位小写MD5值
 */
public static String md5(String src)
```

**使用示例**：

```java
String hash = Crypto.md5("hello world");
// 输出: 5eb63bbbe01eeed093cb22bb8f5acdc3
```

#### AES 加密/解密

```java
/**
 * AES-CBC 解密
 *
 * @param src 加密文本（Base64编码）
 * @param KEY 密钥（16字节）
 * @param IV  初始向量（16字节）
 * @return 解密后的明文
 */
public static String CBC(String src, String KEY, String IV)

/**
 * AES-CBC 加密
 *
 * @param data 明文
 * @param key  密钥（16字节）
 * @param iv   初始向量（16字节）
 * @return Base64编码的密文
 */
public static String aesEncrypt(String data, String key, String iv)
```

**使用示例**：

```java
String key = "1234567890123456";  // 16字节
String iv = "abcdefghijklmnop";   // 16字节

// 加密
String encrypted = Crypto.aesEncrypt("hello", key, iv);

// 解密
String decrypted = Crypto.CBC(encrypted, key, iv);
```

#### RSA 加密/解密

```java
/**
 * RSA 公钥加密
 *
 * @param data         明文
 * @param publicKeyPem 公钥（PEM格式）
 * @return Base64编码的密文
 */
public static String rsaEncrypt(String data, String publicKeyPem)

/**
 * RSA 私钥解密
 *
 * @param encryptedKey  密文（Base64编码）
 * @param privateKeyPem 私钥（PEM格式）
 * @return 解密后的明文
 */
public static String rsaDecrypt(String encryptedKey, String privateKeyPem)
```

#### 生成随机密钥

```java
/**
 * 生成密码学安全的随机密钥
 *
 * @param size 密钥长度
 * @return 随机密钥字符串
 */
public static String randomKey(int size)
```

**使用示例**：

```java
String key = Crypto.randomKey(16);  // 生成16位随机密钥
```

---

## JSON 验证 API

位置：`com.github.catvod.utils.JsonValidator`

#### 验证 JSON 响应

```java
/**
 * 验证并解析JSON响应
 *
 * @param json         JSON字符串
 * @param expectedType 期望类型（"object"或"array"）
 * @return JsonObject，验证失败返回空对象
 * @throws ValidationException 如果JSON无效
 */
public static JsonObject validateResponse(String json, String expectedType)
```

**使用示例**：

```java
try {
    String response = OkHttp.string("https://api.example.com/data");
    JsonObject root = JsonValidator.validateResponse(response, "object");

    // 安全提取字段
    String title = JsonValidator.safeGetString(root, "title", "默认标题");
    int count = JsonValidator.safeGetInt(root, "count", 0);

} catch (ValidationException e) {
    Logger.e("JSON验证失败", e);
}
```

#### 安全字段提取

```java
/**
 * 安全获取字符串字段
 *
 * @param obj          JsonObject
 * @param key          字段名
 * @param defaultValue 默认值
 * @return 字段值或默认值
 */
public static String safeGetString(JsonObject obj, String key, String defaultValue)

/**
 * 安全获取整数字段
 */
public static int safeGetInt(JsonObject obj, String key, int defaultValue)

/**
 * 安全获取布尔字段
 */
public static boolean safeGetBoolean(JsonObject obj, String key, boolean defaultValue)
```

---

## 安全存储 API

位置：`com.github.catvod.utils.SecureStorage`

#### 初始化

```java
/**
 * 初始化加密存储（必须在使用前调用）
 *
 * @param context Android Context
 * @throws SecurityException 如果初始化失败
 */
public static void init(Context context)
```

#### 存储和读取

```java
/**
 * 保存 Cookie
 *
 * @param cookie Cookie字符串
 */
public static void saveCookie(String cookie)

/**
 * 获取 Cookie
 *
 * @return Cookie字符串，不存在返回空字符串
 */
public static String getCookie()

/**
 * 保存 Token
 */
public static void saveToken(String token)

/**
 * 获取 Token
 */
public static String getToken()

/**
 * 清除所有数据
 */
public static void clear()
```

**使用示例**：

```java
// 在 Application.onCreate() 中初始化
SecureStorage.init(this);

// 保存敏感数据
SecureStorage.saveCookie("session=abc123");
SecureStorage.saveToken("bearer_token_xyz");

// 读取
String cookie = SecureStorage.getCookie();
String token = SecureStorage.getToken();

// 清除
SecureStorage.clear();
```

---

## 文件操作 API

位置：`com.github.catvod.utils.Path`

#### 读取文件

```java
/**
 * 读取文件内容
 *
 * @param file 文件对象
 * @return 文件内容（UTF-8编码）
 */
public static String read(File file)

/**
 * 从输入流读取内容
 */
public static String read(InputStream is)
```

#### 写入文件

```java
/**
 * 写入文件
 *
 * @param file 文件对象
 * @param data 字节数据
 * @return 文件对象
 */
public static File write(File file, byte[] data)
```

#### 文件操作

```java
/**
 * 移动文件
 */
public static void move(File in, File out)

/**
 * 复制文件
 */
public static void copy(File in, File out)

/**
 * 删除文件或目录
 */
public static void clear(File dir)

/**
 * 列出目录内容（已排序）
 */
public static List<File> list(File dir)
```

**使用示例**：

```java
// 读取文件
File configFile = new File("/path/to/config.json");
String json = Path.read(configFile);

// 写入文件
byte[] data = "content".getBytes();
Path.write(new File("/path/to/output.txt"), data);

// 列出目录
List<File> files = Path.list(new File("/path/to/dir"));
```

---

## 日志 API

位置：`com.github.catvod.crawler.SpiderDebug`

#### 日志输出

```java
/**
 * 输出普通日志
 *
 * @param msg 日志消息
 */
public static void log(String msg)

/**
 * 输出异常日志
 */
public static void log(Throwable e)

/**
 * 输出脱敏日志（自动隐藏敏感信息）
 */
public static void logSanitized(String msg)
```

**使用示例**：

```java
SpiderDebug.log("开始请求API");

try {
    // ...
} catch (Exception e) {
    SpiderDebug.log(e);
}

// 包含敏感信息的日志
String response = "{\"token\":\"abc123xyz789\"}";
SpiderDebug.logSanitized(response);
// 输出: {"token":"abc1***x789"}
```

---

## 错误处理

所有 API 都遵循以下错误处理原则：

1. **不抛出未检查异常**：所有可能失败的操作都捕获异常
2. **返回安全默认值**：失败时返回空字符串、空对象或 null
3. **记录详细日志**：使用 Logger 记录错误信息和堆栈
4. **不静默失败**：所有异常都会被记录

---

**版本**: 1.0.0
**最后更新**: 2026-02-05
