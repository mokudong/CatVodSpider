package com.github.catvod.api.contract;

import java.util.Map;

/**
 * HTTP 客户端接口契约
 * <p>
 * 定义网络请求的标准接口，使得爬虫可以不依赖具体的 HTTP 实现（如 OkHttp）。
 * 便于单元测试时注入 Mock 实现。
 * </p>
 * <p>
 * <b>设计优势：</b>
 * <ul>
 *   <li>依赖注入：爬虫依赖接口而非具体实现</li>
 *   <li>易于测试：可以注入 MockHttpClient</li>
 *   <li>灵活切换：可以切换不同的 HTTP 库</li>
 * </ul>
 * </p>
 *
 * @author CatVod Team
 * @since 2.0.0
 */
public interface IHttpClient {

    /**
     * 发送 GET 请求
     *
     * @param url 请求URL
     * @return 响应内容
     */
    String get(String url);

    /**
     * 发送 GET 请求（带请求头）
     *
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应内容
     */
    String get(String url, Map<String, String> headers);

    /**
     * 发送 POST 请求（表单）
     *
     * @param url    请求URL
     * @param params 表单参数
     * @return HTTP 响应对象
     */
    HttpResponse post(String url, Map<String, String> params);

    /**
     * 发送 POST 请求（JSON Body）
     *
     * @param url  请求URL
     * @param json JSON字符串
     * @return HTTP 响应对象
     */
    HttpResponse post(String url, String json);

    /**
     * 发送 POST 请求（带请求头）
     *
     * @param url     请求URL
     * @param params  表单参数
     * @param headers 请求头
     * @return HTTP 响应对象
     */
    HttpResponse post(String url, Map<String, String> params, Map<String, String> headers);

    /**
     * HTTP 响应对象
     */
    interface HttpResponse {
        /**
         * 获取状态码
         */
        int getCode();

        /**
         * 获取响应体
         */
        String getBody();

        /**
         * 获取响应头
         */
        Map<String, String> getHeaders();

        /**
         * 是否成功（状态码 2xx）
         */
        default boolean isSuccessful() {
            int code = getCode();
            return code >= 200 && code < 300;
        }
    }
}
