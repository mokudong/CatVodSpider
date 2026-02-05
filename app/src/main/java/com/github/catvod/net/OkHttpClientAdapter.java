package com.github.catvod.net;

import com.github.catvod.api.contract.IHttpClient;

import java.util.Map;

/**
 * OkHttp 客户端适配器
 * <p>
 * 实现 {@link IHttpClient} 接口，适配现有的 {@link OkHttp} 静态方法。
 * 提供依赖注入支持，使得爬虫可以通过接口使用 HTTP 客户端。
 * </p>
 *
 * <h3>设计模式：</h3>
 * <ul>
 *   <li>适配器模式：将 OkHttp 静态方法适配为 IHttpClient 接口</li>
 *   <li>单例模式：全局共享一个实例</li>
 *   <li>依赖倒置：爬虫依赖接口而非具体实现</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 方式1：单例模式
 * IHttpClient httpClient = OkHttpClientAdapter.getInstance();
 * String result = httpClient.get("https://example.com/api");
 *
 * // 方式2：依赖注入
 * public class MySpider extends Spider {
 *     private final IHttpClient httpClient;
 *
 *     public MySpider(IHttpClient httpClient) {
 *         this.httpClient = httpClient;
 *     }
 *
 *     public String searchContent(String keyword, boolean quick) {
 *         String response = httpClient.get("https://api.example.com/search?q=" + keyword);
 *         return response;
 *     }
 * }
 * </pre>
 *
 * <h3>单元测试示例：</h3>
 * <pre>
 * // 注入 Mock 实现进行测试
 * IHttpClient mockHttpClient = mock(IHttpClient.class);
 * when(mockHttpClient.get(anyString())).thenReturn("{\"result\":[]}");
 *
 * MySpider spider = new MySpider(mockHttpClient);
 * String result = spider.searchContent("test", false);
 *
 * verify(mockHttpClient).get(contains("test"));
 * assertNotNull(result);
 * </pre>
 *
 * @author CatVod
 * @version 2.0.0
 * @see IHttpClient
 * @see OkHttp
 * @since 2.0.0
 */
public class OkHttpClientAdapter implements IHttpClient {

    /**
     * 单例实例
     */
    private static final OkHttpClientAdapter INSTANCE = new OkHttpClientAdapter();

    /**
     * 私有构造函数，防止外部实例化
     */
    private OkHttpClientAdapter() {
    }

    /**
     * 获取单例实例
     *
     * @return OkHttpClientAdapter 实例
     */
    public static OkHttpClientAdapter getInstance() {
        return INSTANCE;
    }

    /**
     * 发送 GET 请求
     * <p>
     * 委托给 {@link OkHttp#string(String)}
     * </p>
     *
     * @param url 请求URL
     * @return 响应内容
     */
    @Override
    public String get(String url) {
        return OkHttp.string(url);
    }

    /**
     * 发送 GET 请求（带请求头）
     * <p>
     * 委托给 {@link OkHttp#string(String, Map)}
     * </p>
     *
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应内容
     */
    @Override
    public String get(String url, Map<String, String> headers) {
        return OkHttp.string(url, headers);
    }

    /**
     * 发送 POST 请求（表单）
     * <p>
     * 委托给 {@link OkHttp#post(String, Map, Map)}
     * </p>
     *
     * @param url    请求URL
     * @param params 表单参数
     * @return HTTP 响应对象
     */
    @Override
    public HttpResponse post(String url, Map<String, String> params) {
        return OkHttp.post(url, params, null);
    }

    /**
     * 发送 POST 请求（JSON Body）
     * <p>
     * 委托给 {@link OkHttp#post(String, String, Map)}
     * </p>
     *
     * @param url  请求URL
     * @param json JSON字符串
     * @return HTTP 响应对象
     */
    @Override
    public HttpResponse post(String url, String json) {
        return OkHttp.post(url, json, null);
    }

    /**
     * 发送 POST 请求（带请求头）
     * <p>
     * 委托给 {@link OkHttp#post(String, Map, Map)}
     * </p>
     *
     * @param url     请求URL
     * @param params  表单参数
     * @param headers 请求头
     * @return HTTP 响应对象
     */
    @Override
    public HttpResponse post(String url, Map<String, String> params, Map<String, String> headers) {
        return OkHttp.post(url, params, headers);
    }

    /**
     * 转换为字符串表示
     *
     * @return 类名和实例信息
     */
    @Override
    public String toString() {
        return "OkHttpClientAdapter{singleton}";
    }
}
