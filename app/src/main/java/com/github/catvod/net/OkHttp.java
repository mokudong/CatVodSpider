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
import okhttp3.Dns;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
     * 默认超时时间：15秒
     */
    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(15);

    /**
     * POST 请求方法常量
     */
    public static final String POST = "POST";

    /**
     * GET 请求方法常量
     */
    public static final String GET = "GET";

    /**
     * OkHttpClient 实例（可选，用于自定义配置）
     * 使用 volatile 确保多线程可见性
     */
    private volatile OkHttpClient client;

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
    private static OkHttpClient.Builder getBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dns(safeDns())
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS);

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
     * 获取 OkHttpClient 实例
     * <p>
     * 优先使用 Spider 自定义的 client，如果没有则使用默认配置。
     * </p>
     *
     * @return OkHttpClient 实例
     */
    private static OkHttpClient client() {
        try {
            return Objects.requireNonNull(Spider.client());
        } catch (Throwable e) {
            return build();
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
