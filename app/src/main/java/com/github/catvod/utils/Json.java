package com.github.catvod.utils;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orhanobut.logger.Logger;

/**
 * JSON 解析工具类
 * <p>
 * 基于 Gson 封装的 JSON 解析工具，提供安全、简洁的 JSON 操作方法。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>安全解析 JSON 字符串</li>
 *   <li>容错处理，解析失败时返回空对象</li>
 *   <li>兼容多种 JSON 格式</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 解析 JSON
 * String jsonStr = '{"code":1,"data":{"name":"test"}}';
 * JsonObject obj = Json.parse(jsonStr).getAsJsonObject();
 *
 * // 安全解析（不会抛出异常）
 * JsonObject obj = Json.safeObject(jsonStr);
 * String value = obj.has("data") ? obj.get("data").getAsString() : "";
 *
 * // 获取嵌套字段
 * if (obj.has("data") && !obj.get("data").isJsonNull()) {
 *     JsonObject data = obj.getAsJsonObject("data");
 *     String name = data.get("name").getAsString();
 * }
 * </pre>
 *
 * @author CatVod
 */
public class Json {

    /**
     * 解析 JSON 字符串为 JsonElement
     * <p>
     * 尝试使用 parseString 方法解析（Gson 2.8.0+），
     * 如果失败则使用 parse 方法作为备选。
     * </p>
     *
     * @param json JSON 字符串
     * @return JsonElement 对象
     *
     * <h4>示例：</h4>
     * <pre>
     * String json = '{"code":1,"msg":"success"}';
     * JsonElement element = Json.parse(json);
     *
     * // 转换为 JsonObject
     * JsonObject obj = element.getAsJsonObject();
     * int code = obj.get("code").getAsInt();
     *
     * // 转换为 JsonArray
     * JsonArray array = element.getAsJsonArray();
     * </pre>
     */
    public static JsonElement parse(String json) {
        try {
            return JsonParser.parseString(json);
        } catch (Throwable e) {
            return new JsonParser().parse(json);
        }
    }

    /**
     * 安全解析 JSON 字符串为 JsonObject
     * <p>
     * 此方法不会抛出异常，当解析失败时返回空的 JsonObject。
     * 推荐在处理不确定的 JSON 来源时使用。
     * </p>
     *
     * @param json JSON 字符串
     * @return JsonObject 对象，解析失败时返回空对象
     *
     * <h4>使用场景：</h4>
     * <pre>
     * // 场景1：解析 API 响应
     * String response = OkHttp.string(url);
     * JsonObject obj = Json.safeObject(response);
     * if (obj.has("data") && !obj.get("data").isJsonNull()) {
     *     // 安全处理数据
     * }
     *
     * // 场景2：验证字段存在性
     * JsonObject obj = Json.safeObject(rawString);
     * String value = "";
     * if (obj.has("field") && !obj.get("field").isJsonNull()) {
     *     value = obj.get("field").getAsString();
     * }
     *
     * // 场景3：获取嵌套对象
     * JsonObject obj = Json.safeObject(jsonString);
     * JsonObject data = obj.has("data") ? obj.getAsJsonObject("data") : new JsonObject();
     * </pre>
     */
    public static JsonObject safeObject(String json) {
        try {
            JsonObject obj = parse(json).getAsJsonObject();
            return obj == null ? new JsonObject() : obj;
        } catch (Throwable e) {
            return new JsonObject();
        }
    }

    /**
     * 安全分割字符串
     * <p>
     * 从 JsonObject 中获取字符串字段并按分隔符分割，提供默认值支持。
     * 此方法不会抛出异常，当字段不存在或为空时返回默认值。
     * </p>
     *
     * @param obj       JsonObject 对象
     * @param key       字段名
     * @param delimiter 分隔符
     * @param defaults  默认值（可选，如果不提供则返回空数组）
     * @return 字符串数组
     *
     * <h4>使用示例：</h4>
     * <pre>
     * JsonObject extend = Json.safeObject(extendStr);
     *
     * // 基本用法
     * String[] types = Json.safeStringSplit(extend, "type", "#");
     * // 如果 "type" 不存在或为空，返回 []
     *
     * // 带默认值
     * String[] items = Json.safeStringSplit(extend, "items", "#", "default");
     * // 如果 "items" 不存在或为空，返回 ["default"]
     *
     * // 多个默认值
     * String[] categories = Json.safeStringSplit(extend, "category", ",", "电影", "电视剧");
     * // 如果 "category" 不存在或为空，返回 ["电影", "电视剧"]
     * </pre>
     */
    public static String[] safeStringSplit(JsonObject obj, String key, String delimiter, String... defaults) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return defaults.length > 0 ? defaults : new String[]{};
        }

        try {
            String value = obj.get(key).getAsString();
            if (TextUtils.isEmpty(value)) {
                return defaults.length > 0 ? defaults : new String[]{};
            }
            return value.split(delimiter);
        } catch (Exception e) {
            Logger.w("Failed to split string for key: " + key, e);
            return defaults.length > 0 ? defaults : new String[]{};
        }
    }

    /**
     * 安全获取字符串
     * <p>
     * 从 JsonObject 中安全地获取字符串字段，提供默认值支持。
     * 此方法不会抛出异常。
     * </p>
     *
     * @param obj          JsonObject 对象
     * @param key          字段名
     * @param defaultValue 默认值
     * @return 字符串值，字段不存在或为空时返回默认值
     *
     * <h4>使用示例：</h4>
     * <pre>
     * JsonObject obj = Json.safeObject(jsonStr);
     *
     * String name = Json.safeGetString(obj, "name", "Unknown");
     * String url = Json.safeGetString(obj, "url", "");
     * </pre>
     */
    public static String safeGetString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return defaultValue;
        }

        try {
            return obj.get(key).getAsString();
        } catch (Exception e) {
            Logger.w("Failed to get string for key: " + key, e);
            return defaultValue;
        }
    }

    /**
     * 安全获取整数
     * <p>
     * 从 JsonObject 中安全地获取整数字段，提供默认值支持。
     * 此方法不会抛出异常。
     * </p>
     *
     * @param obj          JsonObject 对象
     * @param key          字段名
     * @param defaultValue 默认值
     * @return 整数值，字段不存在或格式错误时返回默认值
     */
    public static int safeGetInt(JsonObject obj, String key, int defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return defaultValue;
        }

        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            Logger.w("Failed to get int for key: " + key, e);
            return defaultValue;
        }
    }

    /**
     * 安全获取 JsonObject
     * <p>
     * 从 JsonObject 中安全地获取嵌套对象。
     * 此方法不会抛出异常。
     * </p>
     *
     * @param obj JsonObject 对象
     * @param key 字段名
     * @return JsonObject 对象，字段不存在时返回空 JsonObject
     *
     * <h4>使用示例：</h4>
     * <pre>
     * JsonObject response = Json.safeObject(responseStr);
     * JsonObject data = Json.safeGetJsonObject(response, "data");
     *
     * // 继续安全获取嵌套字段
     * String token = Json.safeGetString(data, "token", "");
     * </pre>
     */
    public static JsonObject safeGetJsonObject(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return new JsonObject();
        }

        try {
            return obj.get(key).getAsJsonObject();
        } catch (Exception e) {
            Logger.w("Failed to get JsonObject for key: " + key, e);
            return new JsonObject();
        }
    }

    /**
     * 安全获取 JsonArray
     * <p>
     * 从 JsonObject 中安全地获取数组字段。
     * 此方法不会抛出异常。
     * </p>
     *
     * @param obj JsonObject 对象
     * @param key 字段名
     * @return JsonArray 对象，字段不存在时返回空 JsonArray
     *
     * <h4>使用示例：</h4>
     * <pre>
     * JsonObject response = Json.safeObject(responseStr);
     * JsonArray items = Json.safeGetJsonArray(response, "items");
     *
     * for (JsonElement item : items) {
     *     JsonObject itemObj = item.getAsJsonObject();
     *     // 处理 item
     * }
     * </pre>
     */
    public static JsonArray safeGetJsonArray(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return new JsonArray();
        }

        try {
            return obj.get(key).getAsJsonArray();
        } catch (Exception e) {
            Logger.w("Failed to get JsonArray for key: " + key, e);
            return new JsonArray();
        }
    }
}
