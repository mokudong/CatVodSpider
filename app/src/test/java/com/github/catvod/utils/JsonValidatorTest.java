package com.github.catvod.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JsonValidator 单元测试
 * <p>
 * 测试 JSON 验证和安全字段提取功能。
 * </p>
 *
 * @author CatVod Team
 */
public class JsonValidatorTest {

    @Test
    public void testValidateResponse_validObject() throws Exception {
        String json = "{\"name\":\"test\",\"value\":123}";
        JsonObject result = JsonValidator.validateResponse(json, "object");

        assertNotNull("结果不应为 null", result);
        assertTrue("应该包含 name 字段", result.has("name"));
        assertEquals("name 字段值应为 test", "test", result.get("name").getAsString());
    }

    @Test
    public void testValidateResponse_validArray() throws Exception {
        String json = "[{\"id\":1},{\"id\":2}]";
        JsonObject result = JsonValidator.validateResponse(json, "array");

        assertNotNull("结果不应为 null", result);
        assertTrue("应该包含 list 字段", result.has("list"));

        JsonArray array = result.getAsJsonArray("list");
        assertEquals("数组长度应为 2", 2, array.size());
    }

    @Test(expected = JsonValidator.ValidationException.class)
    public void testValidateResponse_invalidJson() throws Exception {
        String json = "{invalid json";
        JsonValidator.validateResponse(json, "object");
    }

    @Test(expected = JsonValidator.ValidationException.class)
    public void testValidateResponse_typeMismatch() throws Exception {
        // JSON 是 array，但期望 object
        String json = "[1,2,3]";
        JsonValidator.validateResponse(json, "object");
    }

    @Test(expected = JsonValidator.ValidationException.class)
    public void testValidateResponse_tooLarge() throws Exception {
        // 创建超大 JSON（超过 10MB）
        StringBuilder sb = new StringBuilder("{\"data\":\"");
        for (int i = 0; i < 11 * 1024 * 1024; i++) {
            sb.append("a");
        }
        sb.append("\"}");

        JsonValidator.validateResponse(sb.toString(), "object");
    }

    @Test
    public void testSafeGetString_exists() {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", "test");

        String result = JsonValidator.safeGetString(obj, "name", "default");
        assertEquals("应该返回实际值", "test", result);
    }

    @Test
    public void testSafeGetString_notExists() {
        JsonObject obj = new JsonObject();

        String result = JsonValidator.safeGetString(obj, "missing", "default");
        assertEquals("应该返回默认值", "default", result);
    }

    @Test
    public void testSafeGetString_nullValue() {
        JsonObject obj = new JsonObject();
        obj.add("name", null);

        String result = JsonValidator.safeGetString(obj, "name", "default");
        assertEquals("null 值应该返回默认值", "default", result);
    }

    @Test
    public void testSafeGetInt_exists() {
        JsonObject obj = new JsonObject();
        obj.addProperty("count", 123);

        int result = JsonValidator.safeGetInt(obj, "count", 0);
        assertEquals("应该返回实际值", 123, result);
    }

    @Test
    public void testSafeGetInt_notExists() {
        JsonObject obj = new JsonObject();

        int result = JsonValidator.safeGetInt(obj, "missing", 999);
        assertEquals("应该返回默认值", 999, result);
    }

    @Test
    public void testSafeGetInt_invalidType() {
        JsonObject obj = new JsonObject();
        obj.addProperty("count", "not a number");

        int result = JsonValidator.safeGetInt(obj, "count", 0);
        assertEquals("类型不匹配应该返回默认值", 0, result);
    }

    @Test
    public void testSafeGetBoolean_exists() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", true);

        boolean result = JsonValidator.safeGetBoolean(obj, "enabled", false);
        assertTrue("应该返回实际值", result);
    }

    @Test
    public void testSafeGetBoolean_notExists() {
        JsonObject obj = new JsonObject();

        boolean result = JsonValidator.safeGetBoolean(obj, "missing", true);
        assertTrue("应该返回默认值", result);
    }

    @Test
    public void testValidateResponse_emptyObject() throws Exception {
        String json = "{}";
        JsonObject result = JsonValidator.validateResponse(json, "object");

        assertNotNull("结果不应为 null", result);
        assertEquals("应该是空对象", 0, result.size());
    }

    @Test
    public void testValidateResponse_emptyArray() throws Exception {
        String json = "[]";
        JsonObject result = JsonValidator.validateResponse(json, "array");

        assertNotNull("结果不应为 null", result);
        assertTrue("应该包含 list 字段", result.has("list"));

        JsonArray array = result.getAsJsonArray("list");
        assertEquals("数组应该为空", 0, array.size());
    }

    @Test
    public void testValidateResponse_nestedObject() throws Exception {
        String json = "{\"user\":{\"name\":\"test\",\"age\":30}}";
        JsonObject result = JsonValidator.validateResponse(json, "object");

        assertNotNull("结果不应为 null", result);
        assertTrue("应该包含 user 字段", result.has("user"));

        JsonObject user = result.getAsJsonObject("user");
        assertEquals("嵌套对象的 name 应该正确", "test", user.get("name").getAsString());
        assertEquals("嵌套对象的 age 应该正确", 30, user.get("age").getAsInt());
    }

    @Test
    public void testSafeGetString_withSpecialCharacters() {
        JsonObject obj = new JsonObject();
        obj.addProperty("text", "测试中文\n换行\t制表符");

        String result = JsonValidator.safeGetString(obj, "text", "default");
        assertTrue("应该包含中文字符", result.contains("测试中文"));
        assertTrue("应该包含换行符", result.contains("\n"));
    }
}
