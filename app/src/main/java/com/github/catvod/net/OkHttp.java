package com.github.catvod.net;

import android.annotation.SuppressLint;

import com.github.catvod.BuildConfig;
import com.github.catvod.crawler.Spider;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.Dns;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * HTTP 请求工具类
 * <p>
 * 基于 OkHttp 封装的 HTTP 请求工具，提供简洁的 API 用于发送 GET 和 POST 请求。
 * 支持：
 * <ul>
 *   <li>自动管理 Cookie</li>
 *   <li>信任所有 SSL 证书</li>
 *   <li>自定义请求头</li>
 *   <li>请求参数自动拼接</li>
 *   <li>自定义超时时间</li>
 *   <li>请求取消管理</li>
 * </ul>
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 简单 GET 请求
 * String html = OkHttp.string("https://example.com/api/list");
 *
 * // 带请求头的 GET
 * Map&lt;String, String&gt; headers = new HashMap&lt;&gt;();
 * headers.put("User-Agent", "Mozilla/5.0");
 * headers.put("Cookie", "session=xxx");
 * String html = OkHttp.string(url, headers);
 *
 * // 带参数的 GET
 * Map&lt;String, String&gt; params = new HashMap&lt;&gt;();
 * params.put("page", "1");
 * params.put("size", "20");
 * String html = OkHttp.string(url, params, headers);
 *
 * // POST 表单数据
 * Map&lt;String, String&gt; formData = new HashMap&lt;&gt;();
 * formData.put("username", "user");
 * formData.put("password", "pass");
 * String result = OkHttp.post(url, formData);
 *
 * // POST JSON 数据
 * String json = "{\"key\":\"value\"}";
 * String result = OkHttp.post(url, json);
 * </pre>
 *
 * <h3>自定义配置：</h3>
 * <p>
 * 可在 Spider 子类中重写以下方法来自定义配置：
 * </p>
 * <ul>
 *   <li>{@link Spider#client()} - 自定义 OkHttpClient</li>
 *   <li>{@link Spider#safeDns()} - 自定义 DNS 解析器</li>
 * </ul>
 *
 * @author CatVod
 * @see Spider
 * @see OkRequest
 * @see OkResult
 */
public class OkHttp {

    /**
     * 默认超时时间配置
     * <p>
     * 根据不同请求类型设置合理的超时时间：
     * <ul>
     *   <li>连接超时：建立 TCP 连接的最大时间</li>
     *   <li>读取超时：从服务器读取数据的最大时间</li>
     *   <li>写入超时：向服务器写入数据的最大时间</li>
     * </ul>
     * </p>
     */
    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(15);  // 通用请求：15秒

    /**
     * 快速请求超时（用于 API 健康检查等）
     */
    public static final long TIMEOUT_FAST = TimeUnit.SECONDS.toMillis(5);  // 5秒

    /**
     * 慢速请求超时（用于大文件下载、视频流等）
     */
    public static final long TIMEOUT_SLOW = TimeUnit.SECONDS.toMillis(30);  // 30秒

    /**
     * 连接超时（建立 TCP 连接）
     */
    public static final long CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(10);  // 10秒

    /**
     * 调用超时（整个请求完成的最大时间，包括重试）
     */
    public static final long CALL_TIMEOUT = TimeUnit.SECONDS.toMillis(60);  // 60秒

    /**
     * POST 请求方法常量
     */
    public static final String POST = "POST";

    /**
     * GET 请求方法常量
     */
    public static final String GET = "GET";

    /**
     * 最大响应体大小（50MB）
     * <p>
     * 防止下载超大文件导致内存溢出（OOM）。
     * 视频流等大文件应该使用流式处理而非一次性加载到内存。
     * </p>
     */
    private static final long MAX_RESPONSE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * OkHttpClient 实例（可选，用于自定义配置）
     * 使用 volatile 确保多线程可见性
     */
    private volatile OkHttpClient client;

    /**
     * 自定义客户端（用于依赖注入和测试）
     * <p>
     * 通过 {@link #setCustomClient(OkHttpClient)} 设置后，
     * 所有请求将使用此客户端而非默认单例。
     * </p>
     */
    private static volatile OkHttpClient customClient;

    /**
     * 单例加载器（延迟初始化）
     */
    private static class Loader {
        static volatile OkHttp INSTANCE = new OkHttp();
    }

    /**
     * 获取单例实例
     *
     * @return OkHttp 实例
     */
    private static OkHttp get() {
        return Loader.INSTANCE;
    }

    /**
     * 设置自定义客户端（用于依赖注入和测试）
     * <p>
     * <b>用途：</b>
     * <ul>
     *   <li>单元测试：注入 MockWebServer 客户端</li>
     *   <li>集成测试：注入自定义拦截器（日志、重试）</li>
     *   <li>性能测试：注入监控拦截器</li>
     * </ul>
     * </p>
     * <p>
     * <b>示例：单元测试</b>
     * <pre>
     * // 测试前注入 Mock 客户端
     * OkHttpClient mockClient = new OkHttpClient.Builder()
     *     .addInterceptor(new MockInterceptor())
     *     .build();
     * OkHttp.setCustomClient(mockClient);
     *
     * // 执行测试...
     *
     * // 测试后清理
     * OkHttp.resetCustomClient();
     * </pre>
     * </p>
     *
     * @param client 自定义客户端实例，传入 null 则使用默认单例
     */
    public static void setCustomClient(OkHttpClient client) {
        customClient = client;
        Logger.i("Custom OkHttpClient injected for testing/customization");
    }

    /**
     * 重置自定义客户端（测试清理）
     * <p>
     * 测试结束后调用此方法，恢复使用默认单例客户端。
     * </p>
     */
    public static void resetCustomClient() {
        customClient = null;
        // 同时清理单例实例的缓存客户端
        get().client = null;
        Logger.i("Custom OkHttpClient reset, using default singleton");
    }

    /**
     * 发送请求并返回 Response 对象
     * <p>
     * 用于需要访问响应头或状态码的场景。
     * </p>
     *
     * @param url 请求URL
     * @param tag 请求标签，用于取消请求
     * @return Response 对象
     * @throws IOException 请求失败时抛出
     */
    public static Response newCall(String url, String tag) throws IOException {
        return client().newCall(new Request.Builder().url(url).tag(tag).build()).execute();
    }

    /**
     * 简单 GET 请求（无请求头）
     *
     * @param url 请求URL
     * @return 响应内容
     */
    public static String string(String url) {
        return string(url, null);
    }

    /**
     * GET 请求（自定义超时时间）
     *
     * @param url 请求URL
     * @param timeout 超时时间（毫秒）
     * @return 响应内容
     */
    public static String string(String url, long timeout) {
        return string(url, null, null, timeout);
    }

    /**
     * GET 请求（带请求头）
     *
     * @param url 请求URL
     * @param header 请求头，key-value 形式
     * @return 响应内容
     */
    public static String string(String url, Map<String, String> header) {
        return string(url, null, header);
    }

    /**
     * GET 请求（带参数和请求头）
     *
     * @param url 请求URL
     * @param params URL参数，会自动拼接 to URL 后面
     * @param header 请求头
     * @return 响应内容
     *
     * <h4>示例：</h4>
     * <pre>
     * Map&lt;String, String&gt; params = new HashMap&lt;&gt;();
     * params.put("page", "1");
     * params.put("size", "20");
     * Map&lt;String, String&gt; headers = new HashMap&lt;&gt;();
     * headers.put("User-Agent", "Mozilla/5.0");
     * String result = OkHttp.string("https://api.example.com/list", params, headers);
     * // 实际请求：https://api.example.com/list?page=1&size=20
     * </pre>
     */
    public static String string(String url, Map<String, String> params, Map<String, String> header) {
        return new OkRequest(GET, url, params, header).execute(client()).getBody();
    }

    /**
     * GET 请求（带参数、请求头和自定义超时）
     *
     * @param url 请求URL
     * @param params URL参数
     * @param header 请求头
     * @param timeout 超时时间（毫秒）
     * @return 响应内容
     */
    public static String string(String url, Map<String, String> params, Map<String, String> header, long timeout) {
        return new OkRequest(GET, url, params, header).execute(client(timeout)).getBody();
    }

    /**
     * POST 请求（表单数据）
     *
     * @param url 请求URL
     * @param params 表单参数，key-value 形式
     * @return 响应内容
     *
     * <h4>示例：</h4>
     * <pre>
     * Map&lt;String, String&gt; formData = new HashMap&lt;&gt;();
     * formData.put("username", "user");
     * formData.put("password", "pass");
     * String result = OkHttp.post("https://example.com/login", formData);
     * // Content-Type: application/x-www-form-urlencoded
     * </pre>
     */
    public static String post(String url, Map<String, String> params) {
        return post(url, params, null).getBody();
    }

    /**
     * POST 请求（表单数据 + 请求头）
     *
     * @param url 请求URL
     * @param params 表单参数
     * @param header 请求头
     * @return OkResult 包含响应内容和响应头
     */
    public static OkResult post(String url, Map<String, String> params, Map<String, String> header) {
        return new OkRequest(POST, url, params, header).execute(client());
    }

    /**
     * POST 请求（JSON 数据）
     *
     * @param url 请求URL
     * @param json JSON字符串
     * @return 响应内容
     *
     * <h4>示例：</h4>
     * <pre>
     * String json = "{\"keyword\":\"test\",\"page\":1}";
     * String result = OkHttp.post("https://example.com/search", json);
     * // Content-Type: application/json
     * </pre>
     */
    public static String post(String url, String json) {
        return post(url, json, null).getBody();
    }

    /**
     * POST 请求（JSON 数据 + 请求头）
     *
     * @param url 请求URL
     * @param json JSON字符串
     * @param header 请求头
     * @return OkResult 包含响应内容和响应头
     */
    public static OkResult post(String url, String json, Map<String, String> header) {
        return new OkRequest(POST, url, json, header).execute(client());
    }

    /**
     * 获取重定向后的URL
     * <p>
     * 发送请求并自动跟随重定向，返回最终URL。
     * </p>
     *
     * @param url 原始URL
     * @param header 请求头
     * @return 重定向后的URL，如果没有重定向则返回null
     * @throws IOException 请求失败时抛出
     */
    public static String getLocation(String url, Map<String, String> header) throws IOException {
        return getLocation(client().newBuilder().followRedirects(false).followSslRedirects(false).build().newCall(new Request.Builder().url(url).headers(Headers.of(header)).build()).execute().headers().toMultimap());
    }

    /**
     * 从响应头中提取 Location 字段
     *
     * @param headers 响应头
     * @return Location 值，如果不存在则返回null
     */
    public static String getLocation(Map<String, List<String>> headers) {
        if (headers == null) return null;
        if (headers.containsKey("location")) return headers.get("location").get(0);
        if (headers.containsKey("Location")) return headers.get("Location").get(0);
        return null;
    }

    /**
     * 取消指定标签的所有请求
     *
     * @param tag 请求标签
     *
     * <h4>示例：</h4>
     * <pre>
     * // 发起请求时设置标签
     * String url = "https://example.com/api";
     * Response response = OkHttp.newCall(url, "myTag");
     *
     * // 取消所有带有 "myTag" 标签的请求
     * OkHttp.cancel("myTag");
     * </pre>
     */
    public static void cancel(String tag) {
        cancel(client(), tag);
    }

    /**
     * 取消指定 OkHttpClient 中指定标签的所有请求
     *
     * @param client OkHttpClient 实例
     * @param tag 请求标签
     */
    public static void cancel(OkHttpClient client, String tag) {
        for (Call call : client.dispatcher().queuedCalls()) if (tag.equals(call.request().tag())) call.cancel();
        for (Call call : client.dispatcher().runningCalls()) if (tag.equals(call.request().tag())) call.cancel();
    }

    /**
     * 取消所有请求
     */
    public static void cancelAll() {
        cancelAll(client());
    }

    /**
     * 取消指定 OkHttpClient 的所有请求
     *
     * @param client OkHttpClient 实例
     */
    public static void cancelAll(OkHttpClient client) {
        client.dispatcher().cancelAll();
    }

    /**
     * 构建 OkHttpClient 实例（线程安全）
     * <p>
     * 使用双重检查锁定（DCL）模式确保线程安全，避免多次创建实例。
     * </p>
     *
     * @return OkHttpClient 实例
     */
    private static OkHttpClient build() {
        OkHttp instance = get();
        // 第一次检查（无锁，快速路径）
        if (instance.client != null) {
            return instance.client;
        }

        // 同步块（慢速路径）
        synchronized (OkHttp.class) {
            // 第二次检查（防止多个线程同时创建）
            if (instance.client == null) {
                instance.client = getBuilder().build();
                Logger.i("OkHttpClient instance created (thread-safe)");
            }
            return instance.client;
        }
    }

    /**
     * 创建 OkHttpClient.Builder
     * <p>
     * 配置项包括：
     * <ul>
     *   <li>自定义 DNS（从 Spider 获取）</li>
     *   <li>连接超时：15秒</li>
     *   <li>读取超时：15秒</li>
     *   <li>写入超时：15秒</li>
     *   <li>SSL 证书验证（生产环境启用，调试环境可禁用）</li>
     * </ul>
     * </p>
     *
     * @return OkHttpClient.Builder
     */
    /**
     * 创建 OkHttpClient.Builder
     * <p>
     * 配置项包括：
     * <ul>
     *   <li>自定义 DNS（从 Spider 获取）</li>
     *   <li>连接超时：10秒（建立 TCP 连接）</li>
     *   <li>读取超时：15秒（从服务器读取数据）</li>
     *   <li>写入超时：15秒（向服务器写入数据）</li>
     *   <li>调用超时：60秒（整个请求完成时间，包括重试）</li>
     *   <li>响应大小限制：50MB</li>
     *   <li>SSL 证书验证（生产环境启用）</li>
     * </ul>
     * </p>
     *
     * @return OkHttpClient.Builder
     */
    private static OkHttpClient.Builder getBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dns(safeDns())
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .callTimeout(CALL_TIMEOUT, TimeUnit.MILLISECONDS)  // 添加调用超时
                .addInterceptor(responseSizeInterceptor());

        // 仅在生产环境启用证书固定（可选的高级安全特性）
        if (!BuildConfig.DISABLE_SSL_VERIFICATION) {
            CertificatePinner pinner = buildCertificatePinner();
            if (pinner != null) {
                builder.certificatePinner(pinner);
                Logger.i("✓ Certificate Pinning is ENABLED (enhanced security)");
            }
        }

        // 仅在调试模式下禁用 SSL 证书验证
        if (BuildConfig.DISABLE_SSL_VERIFICATION) {
            Logger.w("⚠️ SSL certificate verification is DISABLED (DEBUG BUILD ONLY)");
            Logger.w("⚠️ This is INSECURE and should NEVER be used in production!");
            builder.hostnameVerifier((hostname, session) -> true);
            SSLContext sslContext = getSSLContext();
            if (sslContext != null) {
                builder.sslSocketFactory(sslContext.getSocketFactory(), trustAllCertificates());
            }
        } else {
            // 生产环境：使用系统默认的 SSL 验证
            Logger.i("✓ SSL certificate verification is ENABLED (secure mode)");
        }

        return builder;
    }

    /**
     * 获取自定义超时的 OkHttpClient
     * <p>
     * 用于需要特殊超时配置的场景：
     * <ul>
     *   <li>健康检查：使用 TIMEOUT_FAST（5秒）</li>
     *   <li>大文件下载：使用 TIMEOUT_SLOW（30秒）</li>
     *   <li>视频流请求：使用 TIMEOUT_SLOW（30秒）</li>
     * </ul>
     * </p>
     *
     * @param timeout 超时时间（毫秒）
     * @return OkHttpClient 实例
     */
    private static OkHttpClient client(long timeout) {
        return client().newBuilder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 获取 OkHttpClient 实例（支持依赖注入）
     * <p>
     * 客户端获取优先级：
     * <ol>
     *   <li>自定义客户端（{@link #setCustomClient(OkHttpClient)}）- 最高优先级，用于测试</li>
     *   <li>Spider 自定义客户端（{@link Spider#client()}）- 爬虫自定义配置</li>
     *   <li>默认单例客户端（{@link #build()}）- 默认配置</li>
     * </ol>
     * </p>
     * <p>
     * 这种设计提高了可测试性：
     * <ul>
     *   <li>生产环境：使用默认单例或 Spider 配置</li>
     *   <li>测试环境：通过 setCustomClient() 注入 Mock 客户端</li>
     * </ul>
     * </p>
     *
     * @return OkHttpClient 实例
     */
    private static OkHttpClient client() {
        // 优先级1: 自定义客户端（测试/依赖注入）
        if (customClient != null) {
            return customClient;
        }

        // 优先级2: Spider 自定义客户端
        try {
            return Objects.requireNonNull(Spider.client());
        } catch (Throwable e) {
            // Spider 未提供自定义客户端
        }

        // 优先级3: 默认单例客户端
        return build();
    }

    /**
     * 构建证书固定配置（Certificate Pinning）
     * <p>
     * <b>什么是证书固定？</b><br>
     * 证书固定是一种高级安全技术，通过在应用中硬编码服务器证书的公钥指纹（SHA-256），
     * 防止中间人攻击（MITM），即使攻击者拥有受信任 CA 签发的证书也无法通过验证。
     * </p>
     * <p>
     * <b>使用场景：</b>
     * <ul>
     *   <li>保护关键 API 接口（登录、支付）</li>
     *   <li>防御恶意 CA 证书攻击</li>
     *   <li>企业内部服务（自签名证书）</li>
     * </ul>
     * </p>
     * <p>
     * <b>如何获取证书指纹？</b>
     * <pre>
     * # 方法1: 使用 openssl
     * openssl s_client -connect api.example.com:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
     *
     * # 方法2: 使用 OkHttp 自动生成（推荐）
     * CertificatePinner pinner = new CertificatePinner.Builder()
     *     .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
     *     .build();
     * // 运行后查看日志，OkHttp 会打印实际的证书指纹
     * </pre>
     * </p>
     * <p>
     * <b>配置示例：</b>
     * <pre>
     * CertificatePinner pinner = new CertificatePinner.Builder()
     *     // GitHub API (示例)
     *     .add("api.github.com", "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=")
     *     // 备用证书（证书轮换）
     *     .add("api.github.com", "sha256/RRM1dGqnDFsCJXBTHky16vi1obOlCgFFn/yOhI/y+ho=")
     *     // 其他域名...
     *     .build();
     * </pre>
     * </p>
     * <p>
     * <b>⚠️ 注意事项：</b>
     * <ul>
     *   <li>证书过期或轮换时需要更新指纹（推荐配置多个备用证书）</li>
     *   <li>调试模式下自动禁用（避免影响开发）</li>
     *   <li>指纹错误会导致所有请求失败（请谨慎配置）</li>
     * </ul>
     * </p>
     *
     * @return CertificatePinner 实例，如果未配置返回 null
     */
    private static CertificatePinner buildCertificatePinner() {
        try {
            // TODO: 从配置文件读取证书固定配置
            // 示例: 可以从 Spider.getPinnedCertificates() 获取配置

            // 当前实现: 默认不启用证书固定
            // 如需启用，请参考上方文档添加证书指纹

            /*
            // 示例配置（取消注释后启用）:
            CertificatePinner pinner = new CertificatePinner.Builder()
                // 添加您的 API 域名和证书指纹
                .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                .add("api.example.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")  // 备用证书
                .build();
            return pinner;
            */

            return null;  // 默认不启用
        } catch (Exception e) {
            Logger.e("Failed to build CertificatePinner", e);
            return null;
        }
    }

    /**
     * 获取 DNS 解析器
     * <p>
     * 优先使用 Spider 自定义的 DNS，如果没有则使用系统默认。
     * </p>
     *
     * @return DNS 实例
     */
    private static Dns safeDns() {
        try {
            return Objects.requireNonNull(Spider.safeDns());
        } catch (Throwable e) {
            return Dns.SYSTEM;
        }
    }

    /**
     * 获取 SSL 上下文
     * <p>
     * 创建信任所有证书的 SSL 上下文。
     * </p>
     *
     * @return SSLContext 实例
     */
    private static SSLContext getSSLContext() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{trustAllCertificates()}, new SecureRandom());
            return context;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 创建响应大小限制拦截器
     * <p>
     * 检查响应的 Content-Length 头，如果超过限制则拒绝接收。
     * 防止下载超大文件导致 OOM（内存溢出）。
     * </p>
     *
     * @return 拦截器实例
     */
    private static Interceptor responseSizeInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);

                // 获取 Content-Length（响应体大小）
                ResponseBody body = response.body();
                if (body != null) {
                    long contentLength = body.contentLength();

                    // 检查是否超过限制（-1 表示未知大小，允许通过）
                    if (contentLength > MAX_RESPONSE_SIZE) {
                        // 关闭响应体
                        body.close();

                        // 记录警告日志
                        Logger.w(String.format(
                                "Response size (%d bytes) exceeds limit (%d bytes) for URL: %s",
                                contentLength, MAX_RESPONSE_SIZE, request.url()
                        ));

                        // 抛出异常
                        throw new IOException(String.format(
                                "Response too large: %d bytes (max %d bytes). URL: %s",
                                contentLength, MAX_RESPONSE_SIZE, request.url()
                        ));
                    }

                    // 未知大小的响应打印警告（可能导致 OOM）
                    if (contentLength == -1) {
                        Logger.w("Response size unknown (no Content-Length header) for URL: " + request.url());
                        Logger.w("Risk of OOM if response is too large. Consider using streaming.");
                    }
                }

                return response;
            }
        };
    }

    /**
     * 创建信任所有证书的 TrustManager
     * <p>
     * <b>⚠️ 安全警告：</b>
     * 此方法创建的 TrustManager 会接受任何 SSL/TLS 证书，包括自签名证书、过期证书和伪造证书。
     * 这使得应用完全暴露于中间人攻击 (MITM)。
     * </p>
     * <p>
     * <b>仅在以下情况使用：</b>
     * <ul>
     *   <li>开发环境调试（BuildConfig.DISABLE_SSL_VERIFICATION = true）</li>
     *   <li>本地测试自签名证书</li>
     * </ul>
     * </p>
     * <p>
     * <b>⚠️ 禁止在生产环境使用！</b>
     * 生产环境必须启用完整的证书验证以保护用户数据安全。
     * </p>
     *
     * @return X509TrustManager 实例（不进行任何验证）
     */
    @SuppressLint({"TrustAllX509TrustManager", "CustomX509TrustManager"})
    private static X509TrustManager trustAllCertificates() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // 空实现 = 不验证客户端证书（不安全）
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // 空实现 = 不验证服务器证书（不安全）
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }
}
