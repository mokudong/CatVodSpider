package com.github.catvod.net;

import android.text.TextUtils;

import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Util;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class OkRequest {

    private final Map<String, String> header;
    private final Map<String, String> params;
    private final String method;
    private final String json;
    private Request request;
    private String url;

    OkRequest(String method, String url, Map<String, String> params, Map<String, String> header) {
        this(method, url, params, null, header);
    }

    OkRequest(String method, String url, String json, Map<String, String> header) {
        this(method, url, null, json, header);
    }

    private OkRequest(String method, String url, Map<String, String> params, String json, Map<String, String> header) {
        this.url = url;
        this.json = json;
        this.method = method;
        this.params = params;
        this.header = header;
        this.buildRequest();
    }

    private void buildRequest() {
        Request.Builder builder = new Request.Builder();
        if (method.equals(OkHttp.GET) && params != null) setParams();
        if (method.equals(OkHttp.POST)) builder.post(getRequestBody());
        if (header != null) for (String key : header.keySet()) builder.addHeader(key, header.get(key));
        request = builder.url(url).build();
    }

    private RequestBody getRequestBody() {
        if (!TextUtils.isEmpty(json)) return RequestBody.create(MediaType.get("application/json; charset=utf-8"), json);
        FormBody.Builder formBody = new FormBody.Builder();
        if (params != null) for (String key : params.keySet()) formBody.add(key, params.get(key));
        return formBody.build();
    }

    /**
     * 构建 GET 请求的 URL 查询参数
     * <p>
     * 使用 URLEncoder 对参数进行编码，防止特殊字符导致 URL 错误。
     * 使用 StringBuilder 提高字符串拼接效率。
     * </p>
     */
    private void setParams() {
        if (params == null || params.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder(url);
        sb.append("?");

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            first = false;

            try {
                // URL 编码：防止特殊字符（&, =, 空格, 中文等）破坏 URL 结构
                String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
                String encodedValue = URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", "UTF-8");
                sb.append(encodedKey).append("=").append(encodedValue);
            } catch (UnsupportedEncodingException e) {
                // UTF-8 是标准编码，理论上不会抛出此异常
                Logger.e("Failed to encode URL parameter: " + entry.getKey(), e);
                // 降级处理：不编码直接拼接（可能导致 URL 错误，但至少不会崩溃）
                sb.append(entry.getKey()).append("=").append(entry.getValue() != null ? entry.getValue() : "");
            }
        }

        url = sb.toString();
    }

    public OkResult execute(OkHttpClient client) {
        try (Response res = client.newCall(request).execute()) {
            ResponseBody body = res.body();
            if (body == null) {
                Logger.e("Response body is null for URL: " + url);
                return new OkResult(res.code(), "", res.headers().toMultimap());
            }
            return new OkResult(res.code(), body.string(), res.headers().toMultimap());
        } catch (IOException e) {
            Logger.e("Network request failed for URL: " + url, e);
            SpiderDebug.log(e);
            return new OkResult();
        } catch (Exception e) {
            Logger.e("Unexpected error during request for URL: " + url, e);
            return new OkResult();
        }
    }
}
