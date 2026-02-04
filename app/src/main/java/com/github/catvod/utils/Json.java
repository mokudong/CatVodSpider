package com.github.catvod.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
}
