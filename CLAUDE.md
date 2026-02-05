# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 项目概述

**CatVodSpider** 是 CatVod 爬虫框架的独立实现，用于为 TVBox 应用提供视频源爬虫插件。

基于: https://github.com/CatVodTVOfficial/CatVodTVSpider

**核心功能**:
- 爬虫基类框架 (`Spider.java`)
- 内置多个常用视频源爬虫实现 (Bili, AList, Jianpian, WebDAV, Samba 等)
- JavaScript 扩展支持 (QuickJS)
- 网络请求封装 (OkHttp)
- 编译打包为 JAR 插件供 TVBox 使用

---

## 构建和打包

### 前置要求

- JDK 17
- Android SDK (API Level 36)
- Gradle 9.0.0 (通过 wrapper 管理)
- apktool 2.11.0 (位于 `jar/3rd/` 目录)

### 构建命令

```bash
# 清理构建
./gradlew clean

# 编译调试版 APK
./gradlew assembleDebug

# 编译发布版 APK (包含 ProGuard 混淆)
./gradlew assembleRelease

# 一键构建发布版 APK 并生成 JAR
./build.bat       # Windows
./gradlew assembleRelease && ./jar/genJar.bat   # 手动执行

# 仅生成 JAR (需要先编译发布版 APK)
./jar/genJar.bat
```

### JAR 生成流程

执行 `build.bat` 后的完整流程：

1. **编译发布版 APK**:
   ```bash
   ./gradlew assembleRelease --no-daemon
   ```
   输出: `app/build/outputs/apk/release/app-release-unsigned.apk`

2. **反编译 APK 为 Smali**:
   使用 apktool 解包 APK，提取爬虫相关类的 Smali 代码

3. **合并到 JAR 模板**:
   - 提取 `com/github/catvod/spider/` (爬虫实现)
   - 提取 `com/github/catvod/js/` (JavaScript 扩展)
   - 提取 `org/slf4j/` (日志框架)
   - 合并到 `jar/spider.jar/` 模板

4. **重新打包为 DEX JAR**:
   使用 apktool 将 Smali 编译为 `dex.jar`

5. **生成最终 JAR**:
   - 输出: `jar/custom_spider.jar`
   - 计算 MD5: `jar/custom_spider.jar.md5`

### 输出文件

```
jar/
├── custom_spider.jar        # 最终爬虫 JAR 包
├── custom_spider.jar.md5    # MD5 校验和
└── 3rd/
    └── apktool_2.11.0.jar   # APK 反编译工具
```

---

## 核心架构

### 模块结构

```
app/src/main/java/com/github/catvod/
├── crawler/          # 爬虫框架核心
│   ├── Spider.java           # 爬虫抽象基类 (所有爬虫必须继承)
│   └── SpiderDebug.java      # 爬虫调试工具
│
├── spider/           # 内置爬虫实现
│   ├── Init.java             # 全局初始化和线程池管理
│   ├── Proxy.java            # HTTP 代理服务
│   ├── AList.java            # AList 网盘爬虫
│   ├── Bili.java             # Bilibili 爬虫
│   ├── Jianpian.java         # 简片 P2P 爬虫
│   ├── WebDAV.java           # WebDAV 网盘爬虫
│   ├── Samba.java            # SMB 共享爬虫
│   ├── Local.java            # 本地文件爬虫
│   ├── Market.java           # 应用市场爬虫
│   ├── Push.java             # 推送播放爬虫
│   ├── XtreamCode.java       # Xtream Codes IPTV
│   ├── MQiTV.java            # 酒店 IPTV 爬虫
│   ├── PTT.java              # PTT 论坛爬虫
│   ├── Jable.java            # Jable 爬虫
│   ├── Kanqiu.java           # 看球爬虫
│   └── YHDM.java             # 樱花动漫爬虫
│
├── js/               # JavaScript 扩展框架
│   ├── Function.java         # QuickJS 函数封装
│   ├── bean/                 # JS 相关数据模型
│   └── utils/                # JS 工具类
│
├── net/              # 网络请求封装
│   ├── OkHttp.java           # OkHttp 客户端封装
│   ├── OkRequest.java        # 请求构建器
│   └── OkResult.java         # 响应封装
│
├── bean/             # 数据模型
│   ├── Class.java            # 分类模型
│   ├── Filter.java           # 筛选条件模型
│   ├── Danmaku.java          # 弹幕模型
│   ├── alist/                # AList 相关模型
│   ├── bili/                 # Bilibili 相关模型
│   ├── jianpian/             # 简片相关模型
│   ├── webdav/               # WebDAV 相关模型
│   ├── samba/                # Samba 相关模型
│   ├── xtream/               # Xtream Codes 相关模型
│   ├── mqitv/                # MQiTV 相关模型
│   ├── live/                 # 直播相关模型
│   └── market/               # 应用市场相关模型
│
└── utils/            # 工具类
    ├── Utils.java            # 通用工具
    ├── Path.java             # 路径处理
    ├── Json.java             # JSON 工具
    └── ... (其他工具类)
```

---

## 爬虫框架 (Spider)

### Spider 基类

位置: `app/src/main/java/com/github/catvod/crawler/Spider.java`

**所有爬虫必须继承此类并实现以下方法**:

#### 核心方法

| 方法 | 说明 | 返回格式 |
|------|------|----------|
| `init(Context, String)` | 初始化爬虫，解析配置参数 | void |
| `homeContent(boolean)` | 获取首页分类和筛选条件 | JSON |
| `categoryContent(tid, pg, filter, extend)` | 获取分类视频列表 | JSON |
| `detailContent(List<String>)` | 获取视频详情和播放地址 | JSON |
| `searchContent(key, quick, pg)` | 搜索视频 | JSON |
| `playerContent(flag, id, vipFlags)` | 获取实际播放地址 | JSON |
| `destroy()` | 销毁爬虫，释放资源 | void |

#### 可选方法

| 方法 | 说明 |
|------|------|
| `homeVideoContent()` | 获取首页推荐视频 |
| `liveContent(String)` | 获取直播内容 |
| `manualVideoCheck()` | 是否需要手动视频检测 |
| `isVideoFormat(String)` | 判断 URL 是否为视频 |
| `proxy(Map<String, String>)` | 处理代理请求 |
| `action(String)` | 处理自定义操作 |

#### 静态工厂方法

| 方法 | 说明 |
|------|------|
| `safeDns()` | 返回自定义 DNS 解析器 |
| `client()` | 返回自定义 OkHttpClient |

### 返回数据格式

**首页内容** (`homeContent`):
```json
{
  "class": [
    {"type_id": "1", "type_name": "电影"},
    {"type_id": "2", "type_name": "电视剧"}
  ],
  "filters": {
    "1": [
      {"key": "year", "name": "年份", "value": [...]}
    ]
  }
}
```

**分类/搜索列表** (`categoryContent`, `searchContent`):
```json
{
  "list": [
    {
      "vod_id": "123",
      "vod_name": "视频名称",
      "vod_pic": "封面URL",
      "vod_remarks": "备注"
    }
  ],
  "page": 1,
  "pagecount": 10,
  "limit": 20,
  "total": 200
}
```

**视频详情** (`detailContent`):
```json
{
  "list": [{
    "vod_id": "123",
    "vod_name": "视频名称",
    "vod_pic": "封面URL",
    "vod_content": "剧情简介",
    "vod_play_from": "播放源1$$$播放源2",
    "vod_play_url": "第1集$url1#第2集$url2$$$..."
  }]
}
```

**播放地址格式说明**:
- `$$$` - 分隔不同播放源
- `#` - 分隔同一源的不同集数
- `$` - 分隔集数名称和播放地址

**播放信息** (`playerContent`):
```json
{
  "url": "https://example.com/video.m3u8",
  "header": "{\"User-Agent\": \"Mozilla/5.0...\"}"
}
```

---

## 开发新爬虫

### 步骤 1: 创建爬虫类

在 `app/src/main/java/com/github/catvod/spider/` 创建新类：

```java
package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import android.content.Context;
import java.util.List;
import java.util.HashMap;

public class MySpider extends Spider {

    @Override
    public void init(Context context, String extend) throws Exception {
        super.init(context, extend);
        // 初始化配置 (解析 extend 参数)
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        // 返回分类列表
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter,
                                   HashMap<String, String> extend) throws Exception {
        // 返回视频列表
        return "";
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        // 返回视频详情
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        // 返回搜索结果
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        // 返回播放地址
        return "";
    }
}
```

### 步骤 2: 使用网络请求

使用 `OkHttp.java` 进行网络请求：

```java
import com.github.catvod.net.OkHttp;

// GET 请求
String html = OkHttp.string("https://example.com");

// POST 请求
String result = OkHttp.post("https://api.example.com", "param=value");

// 带请求头
OkRequest request = OkHttp.newRequest("https://example.com")
    .header("User-Agent", "Mozilla/5.0")
    .header("Cookie", "session=xxx");
String response = request.get();
```

### 步骤 3: 解析 HTML

使用 JSoup 解析：

```java
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

Document doc = Jsoup.parse(html);
String title = doc.select("h1.title").text();
```

### 步骤 4: 返回 JSON

使用 Gson 构建 JSON：

```java
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

Gson gson = new Gson();
JsonObject result = new JsonObject();
result.addProperty("page", 1);
result.add("list", jsonArray);
return gson.toJson(result);
```

### 步骤 5: 配置文件

在 `json/config.json` 添加配置：

```json
{
  "sites": [
    {
      "key": "myspider",
      "name": "My Spider",
      "type": 3,
      "api": "csp_MySpider",
      "searchable": 1,
      "changeable": 1,
      "ext": "{\"param\":\"value\"}"
    }
  ]
}
```

### 步骤 6: 编译和测试

```bash
# 编译并生成 JAR
./build.bat

# 使用生成的 JAR
# 将 jar/custom_spider.jar 部署到 TVBox 应用
```

---

## 内置爬虫说明

### 网盘类爬虫

**AList** (`AList.java`):
- 支持 AList 网盘协议
- 配置参数: 服务器地址、用户名、密码
- 支持目录浏览和文件播放

**WebDAV** (`WebDAV.java`):
- 支持 WebDAV 协议
- 配置参数: 服务器地址、用户名、密码
- 使用 Sardine 库实现

**Samba** (`Samba.java`):
- 支持 SMB/CIFS 协议
- 配置参数: 服务器地址、共享路径、凭据
- 使用 SMBJ 库实现

### 视频网站爬虫

**Bili** (`Bili.java`):
- Bilibili 视频爬虫
- 支持用户投稿、收藏、历史记录
- 需要登录凭证 (Cookie)

**Jianpian** (`Jianpian.java`):
- 简片 P2P 爬虫
- 支持简片协议视频源

**YHDM** (`YHDM.java`):
- 樱花动漫爬虫
- 动漫视频源

### 直播类爬虫

**XtreamCode** (`XtreamCode.java`):
- Xtream Codes IPTV 协议
- 支持直播和 VOD
- 配置参数: API URL、用户名、密码

**MQiTV** (`MQiTV.java`):
- 酒店 IPTV 系统
- 支持多个酒店配置

### 特殊功能爬虫

**Local** (`Local.java`):
- 本地文件浏览
- 支持扫描设备存储

**Push** (`Push.java`):
- 推送播放功能
- 接收外部推送的视频链接

**Market** (`Market.java`):
- 应用市场爬虫
- 用于插件管理

**Proxy** (`Proxy.java`):
- HTTP 代理服务
- 端口: 7777
- 用于处理特殊播放协议

---

## JavaScript 扩展

### QuickJS 集成

位置: `app/src/main/java/com/github/catvod/js/`

库: `wang.harlon.quickjs:wrapper-android:3.2.3`

**功能**: 允许使用 JavaScript 实现爬虫逻辑

**使用方式**:
1. 创建 JS 文件实现 Spider 接口
2. 使用 `Function.java` 执行 JS 代码
3. 与 Java 层交互

---

## 网络请求 (OkHttp)

### OkHttp 封装

位置: `app/src/main/java/com/github/catvod/net/OkHttp.java`

基于: OkHttp 5.3.2

**特性**:
- 自动管理 Cookie
- 支持自定义 DNS (DoH)
- 超时配置
- SSL/TLS 配置
- 代理支持

**常用方法**:

```java
// 简单 GET 请求
String html = OkHttp.string(url);

// POST 请求
String result = OkHttp.post(url, body);

// 构建复杂请求
OkRequest request = OkHttp.newRequest(url)
    .header("User-Agent", "...")
    .header("Referer", "...")
    .cookie("session=xxx");
String response = request.get();

// 下载文件
byte[] data = OkHttp.bytes(url);
```

---

## 配置文件

### 主配置

位置: `json/config.json`

**配置项说明**:

```json
{
  "spider": "../jar/custom_spider.jar",    // JAR 包路径
  "sites": [...],                          // 点播源配置
  "lives": [...],                          // 直播源配置
  "doh": [...],                            // DNS over HTTPS 配置
  "rules": [...],                          // 嗅探规则
  "ads": [...]                             // 广告域名屏蔽
}
```

### Site 配置

```json
{
  "key": "spider_key",           // 唯一标识
  "name": "显示名称",
  "type": 3,                     // 类型: 3=爬虫
  "api": "csp_ClassName",        // 爬虫类名 (去掉包名前缀)
  "searchable": 1,               // 是否支持搜索
  "changeable": 1,               // 是否支持切换
  "ext": "配置参数",             // 传递给 init() 的 extend 参数
  "timeout": 30                  // 超时时间 (秒)
}
```

### 爬虫类名映射

配置中的 `api` 字段与实际类名的对应关系：

| 配置值 | 实际类名 |
|--------|----------|
| `csp_AList` | `com.github.catvod.spider.AList` |
| `csp_Bili` | `com.github.catvod.spider.Bili` |
| `csp_WebDAV` | `com.github.catvod.spider.WebDAV` |
| `csp_Samba` | `com.github.catvod.spider.Samba` |

---

## ProGuard 混淆

### 混淆规则

位置: `app/proguard-rules.pro`

**关键保留规则**:

```proguard
# 保留 Spider 基类和所有爬虫类
-keep class com.github.catvod.crawler.* { *; }
-keep class com.github.catvod.spider.* { public <methods>; }

# 保留 JavaScript 扩展
-keep class com.github.catvod.js.Function { *; }

# 保留日志框架
-keep class org.slf4j.** { *; }

# 保留网络库
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# 保留 QuickJS
-keep class com.whl.quickjs.** { *; }

# 保留 WebDAV/Samba 库
-keep class com.thegrizzlylabs.sardineandroid.** { *; }
-keep class com.hierynomus.** { *; }
```

**包扁平化**:
```proguard
-flattenpackagehierarchy com.github.catvod.spider.merge
```

所有爬虫类会被混淆到 `com.github.catvod.spider.merge` 包下。

---

## 依赖管理

### 主要依赖

在 `app/build.gradle` 中定义：

```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:5.3.2'           // HTTP 客户端
    implementation 'wang.harlon.quickjs:wrapper-android:3.2.3'   // QuickJS 引擎
    implementation 'com.google.code.gson:gson:2.13.2'            // JSON 序列化
    implementation 'org.jsoup:jsoup:1.22.1'                      // HTML 解析
    implementation 'com.github.thegrizzlylabs:sardine-android:0.9' // WebDAV
    implementation 'com.hierynomus:smbj:0.14.0'                  // SMB
    implementation 'com.orhanobut:logger:2.2.0'                  // 日志
}
```

### 依赖版本管理

在根 `build.gradle` 中集中管理：

```gradle
project.ext {
    okhttpVersion = '5.3.2'
}
```

---

## 调试和测试

### 调试爬虫

使用 `SpiderDebug.java` 调试：

```java
import com.github.catvod.crawler.SpiderDebug;

// 在 Android 设备上运行
SpiderDebug debug = new SpiderDebug();
debug.test(MySpider.class);
```

### 日志输出

使用 Logger 库：

```java
import com.orhanobut.logger.Logger;

Logger.d("Debug message");
Logger.e("Error message");
Logger.json(jsonString);
```

### 测试配置

Demo 应用在 `app/src/main/java/com/github/catvod/MainActivity.java`

可以在 Android Studio 中直接运行测试爬虫。

---

## 常见问题

### JAR 生成失败

**问题**: `genJar.bat` 找不到 APK 文件

**解决**: 确保先执行 `./gradlew assembleRelease`

### 爬虫无法加载

**问题**: TVBox 加载 JAR 后找不到爬虫类

**解决**:
1. 检查类名是否正确 (必须以 `csp_` 开头)
2. 检查 ProGuard 规则是否保留了爬虫类
3. 查看 JAR 中是否包含对应的类

### 网络请求失败

**问题**: SSL 证书验证失败

**解决**: 在 OkHttp 配置中添加证书信任

### QuickJS 执行错误

**问题**: JavaScript 代码执行异常

**解决**:
1. 检查 JS 语法是否正确
2. 检查是否正确导入依赖
3. 查看 QuickJS 日志输出

---

## 开发规范

### 命名约定

- **爬虫类名**: 首字母大写驼峰命名 (如 `MySpider`)
- **配置 key**: 小写字母+下划线 (如 `my_spider`)
- **API 标识**: `csp_` 前缀 + 类名 (如 `csp_MySpider`)

### 异常处理

- 所有方法可以抛出 `Exception`
- 使用 Logger 记录详细错误信息
- 不要静默吞掉异常

### 资源管理

- 在 `destroy()` 方法中释放资源
- 关闭网络连接
- 清理缓存

### 性能优化

- 使用 `Init.execute()` 执行异步任务
- 避免在主线程进行网络请求
- 合理使用缓存减少请求次数

---

## 项目依赖

**Android Gradle Plugin**: 9.0.0
**Compile SDK**: 36
**Min SDK**: 21
**Target SDK**: 36
**Java Version**: 17

**关键库**:
- OkHttp: 5.3.2
- QuickJS: 3.2.3
- Gson: 2.13.2
- JSoup: 1.22.1
- Sardine-Android: 0.9
- SMBJ: 0.14.0
- Logger: 2.2.0
