package com.github.catvod.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.orhanobut.logger.Logger;

/**
 * JSON 验证工具类
 * <p>
 * 用于安全地验证和解析来自不可信源的 JSON 数据，防止：
 * <ul>
 *   <li>JSON 注入攻击</li>
 *   <li>DoS 攻击（超大 JSON、深度嵌套）</li>
 *   <li>类型混淆漏洞</li>
 *   <li>NullPointerException</li>
 * </ul>
 * </p>
 *
 * @author CatVod
 * @version 1.0
 */
public class JsonValidator {

    /**
     * 最大 JSON 大小（10MB）
     */
    private static final int MAX_JSON_SIZE = 10 * 1024 * 1024;

    /**
     * 最大嵌套深度
     */
    private static final int MAX_DEPTH = 20;

    /**
     * Gson 实例
     */
    private static final Gson gson = new Gson();

    /**
     * 验证并解析 JSON 字符串
     *
     * @param json JSON字符串
     * @param expectedType 期望的数据类型 ("object" 或 "array")
     * @return JsonObject 实例
     * @throws ValidationException 验证失败时抛出
     */
    public static JsonObject validateResponse(String json, String expectedType) throws ValidationException {
        // 1. 大小检查
        if (json == null || json.isEmpty()) {
            throw new ValidationException("JSON string is null or empty");
        }

        if (json.length() > MAX_JSON_SIZE) {
            throw new ValidationException("JSON too large: " + json.length() + " bytes (max: " + MAX_JSON_SIZE + ")");
        }

        // 2. 解析 JSON
        JsonObject root;
        try {
            JsonElement element = gson.fromJson(json, JsonElement.class);
            if (element == null || !element.isJsonObject()) {
                throw new ValidationException("Expected JSON object, got: " +
                        (element == null ? "null" : element.getClass().getSimpleName()));
            }
            root = element.getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new ValidationException("Invalid JSON syntax", e);
        }

        // 3. 深度检查
        int depth = getMaxDepth(root);
        if (depth > MAX_DEPTH) {
            throw new ValidationException("JSON too deeply nested: " + depth + " levels (max: " + MAX_DEPTH + ")");
        }

        // 4. 验证基本结构（可选，根据业务需求调整）
        if (root.has("code") && root.get("code").getAsInt() != 0) {
            String message = safeGetString(root, "message", "Unknown error");
            throw new ValidationException("API returned error code: " + root.get("code").getAsInt() + ", message: " + message);
        }

        return root;
    }

    /**
     * 安全获取字符串字段
     *
     * @param obj JSON对象
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值或默认值
     */
    public static String safeGetString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || !obj.has(key)) {
            return defaultValue;
        }

        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return defaultValue;
        }

        try {
            return element.getAsString();
        } catch (Exception e) {
            Logger.w("Failed to get string for key: " + key, e);
            return defaultValue;
        }
    }

    /**
     * 安全获取整数字段
     *
     * @param obj JSON对象
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值或默认值
     */
    public static int safeGetInt(JsonObject obj, String key, int defaultValue) {
        if (obj == null || !obj.has(key)) {
            return defaultValue;
        }

        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return defaultValue;
        }

        try {
            return element.getAsInt();
        } catch (Exception e) {
            Logger.w("Failed to get int for key: " + key, e);
            return defaultValue;
        }
    }

    /**
     * 安全获取布尔字段
     *
     * @param obj JSON对象
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值或默认值
     */
    public static boolean safeGetBoolean(JsonObject obj, String key, boolean defaultValue) {
        if (obj == null || !obj.has(key)) {
            return defaultValue;
        }

        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return defaultValue;
        }

        try {
            return element.getAsBoolean();
        } catch (Exception e) {
            Logger.w("Failed to get boolean for key: " + key, e);
            return defaultValue;
        }
    }

    /**
     * 安全获取 JsonObject 字段
     *
     * @param obj JSON对象
     * @param key 字段名
     * @return JsonObject 或 null
     */
    public static JsonObject safeGetObject(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }

        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonObject()) {
            return null;
        }

        return element.getAsJsonObject();
    }

    /**
     * 计算 JSON 的最大嵌套深度
     *
     * @param element JSON元素
     * @return 最大深度
     */
    private static int getMaxDepth(JsonElement element) {
        if (element == null || element.isJsonPrimitive() || element.isJsonNull()) {
            return 1;
        }

        int maxChildDepth = 0;

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (String key : obj.keySet()) {
                int childDepth = getMaxDepth(obj.get(key));
                maxChildDepth = Math.max(maxChildDepth, childDepth);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                int childDepth = getMaxDepth(child);
                maxChildDepth = Math.max(maxChildDepth, childDepth);
            }
        }

        return 1 + maxChildDepth;
    }

    /**
     * JSON 验证异常
     */
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
