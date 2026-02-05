package com.github.catvod.net;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

/**
 * OkHttp 工具类单元测试
 * <p>
 * 使用 MockWebServer 测试网络请求功能。
 * </p>
 *
 * @author CatVod Team
 */
public class OkHttpTest {

    private MockWebServer mockServer;
    private String baseUrl;

    @Before
    public void setUp() throws IOException {
        // 启动 Mock 服务器
        mockServer = new MockWebServer();
        mockServer.start();
        baseUrl = mockServer.url("/").toString();

        // 注入自定义客户端（用于测试）
        OkHttpClient testClient = new OkHttpClient.Builder()
                .build();
        OkHttp.setCustomClient(testClient);
    }

    @After
    public void tearDown() throws IOException {
        // 清理
        OkHttp.resetCustomClient();
        mockServer.shutdown();
    }

    @Test
    public void testString_simpleGet() throws Exception {
        // 准备响应
        mockServer.enqueue(new MockResponse()
                .setBody("Hello, World!")
                .setResponseCode(200));

        // 发送请求
        String result = OkHttp.string(baseUrl);

        // 验证结果
        assertEquals("响应内容应该正确", "Hello, World!", result);

        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("应该是 GET 请求", "GET", request.getMethod());
    }

    @Test
    public void testString_withHeaders() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("Success")
                .setResponseCode(200));

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "TestAgent");
        headers.put("Authorization", "Bearer token123");

        String result = OkHttp.string(baseUrl, headers);

        assertEquals("响应内容应该正确", "Success", result);

        // 验证请求头
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("User-Agent 应该正确", "TestAgent", request.getHeader("User-Agent"));
        assertEquals("Authorization 应该正确", "Bearer token123", request.getHeader("Authorization"));
    }

    @Test
    public void testPost_withParams() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("{\"success\":true}")
                .setResponseCode(200));

        Map<String, String> params = new HashMap<>();
        params.put("username", "test");
        params.put("password", "123456");

        OkResult result = OkHttp.post(baseUrl, params);

        assertEquals("状态码应该是 200", 200, result.getCode());
        assertTrue("响应体应该包含 success", result.getBody().contains("success"));

        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("应该是 POST 请求", "POST", request.getMethod());
        assertNotNull("应该有请求体", request.getBody());
    }

    @Test
    public void testPost_withJson() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("{\"result\":\"ok\"}")
                .setResponseCode(200));

        String json = "{\"name\":\"test\",\"value\":123}";
        OkResult result = OkHttp.post(baseUrl, json);

        assertEquals("状态码应该是 200", 200, result.getCode());
        assertTrue("响应体应该包含 result", result.getBody().contains("result"));

        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("Content-Type 应该是 JSON", "application/json; charset=utf-8", request.getHeader("Content-Type"));
    }

    @Test
    public void testString_404Error() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        String result = OkHttp.string(baseUrl);

        // OkHttp.string() 不抛出异常，返回响应体
        assertEquals("应该返回 404 响应体", "Not Found", result);
    }

    @Test
    public void testString_emptyResponse() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(""));

        String result = OkHttp.string(baseUrl);

        assertEquals("空响应应该返回空字符串", "", result);
    }

    @Test
    public void testPost_largePayload() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("OK"));

        // 创建大的 JSON payload
        StringBuilder largeJson = new StringBuilder("{\"data\":\"");
        for (int i = 0; i < 10000; i++) {
            largeJson.append("x");
        }
        largeJson.append("\"}");

        OkResult result = OkHttp.post(baseUrl, largeJson.toString());

        assertEquals("大 payload 应该成功发送", 200, result.getCode());
    }

    @Test
    public void testCustomClient_injection() {
        // 测试依赖注入功能
        OkHttpClient customClient = new OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        OkHttp.setCustomClient(customClient);

        // 验证注入成功（通过发送请求）
        mockServer.enqueue(new MockResponse().setBody("Test"));
        String result = OkHttp.string(baseUrl);

        assertEquals("自定义客户端应该正常工作", "Test", result);
    }

    @Test
    public void testResetCustomClient() {
        OkHttpClient customClient = new OkHttpClient.Builder().build();
        OkHttp.setCustomClient(customClient);

        OkHttp.resetCustomClient();

        // 重置后应该使用默认客户端（测试不会失败即表示成功）
        mockServer.enqueue(new MockResponse().setBody("OK"));
        String result = OkHttp.string(baseUrl);

        assertEquals("重置后应该正常工作", "OK", result);
    }
}
