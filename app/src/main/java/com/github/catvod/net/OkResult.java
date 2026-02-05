package com.github.catvod.net;

import android.text.TextUtils;

import com.github.catvod.api.contract.IHttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 响应结果封装
 * <p>
 * 实现了 {@link IHttpClient.HttpResponse} 接口，遵循依赖倒置原则。
 * </p>
 *
 * @author CatVod
 * @see IHttpClient.HttpResponse
 */
public class OkResult implements IHttpClient.HttpResponse {

    private final int code;
    private final String body;
    private final Map<String, List<String>> resp;

    public OkResult() {
        this.code = 500;
        this.body = "";
        this.resp = new HashMap<>();
    }

    public OkResult(int code, String body, Map<String, List<String>> resp) {
        this.code = code;
        this.body = body;
        this.resp = resp;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getBody() {
        return TextUtils.isEmpty(body) ? "" : body;
    }

    /**
     * 获取响应头（多值格式）
     * <p>
     * 原始的响应头格式，一个 header 可能有多个值。
     * </p>
     *
     * @return 响应头 Map，键为 header 名称，值为 header 值列表
     */
    public Map<String, List<String>> getResp() {
        return resp;
    }

    /**
     * 获取响应头（单值格式）
     * <p>
     * 实现 {@link IHttpClient.HttpResponse#getHeaders()} 接口方法。
     * 如果一个 header 有多个值，只取第一个值。
     * </p>
     *
     * @return 响应头 Map，键为 header 名称，值为 header 值（单值）
     */
    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (resp != null) {
            for (Map.Entry<String, List<String>> entry : resp.entrySet()) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    headers.put(entry.getKey(), values.get(0));
                }
            }
        }
        return headers;
    }
}
