package com.github.catvod.crawler;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬虫调试日志工具类
 * <p>
 * 提供统一的日志输出功能，所有爬虫的调试信息都应通过此类输出。
 * 日志可通过 Android Logcat 查看，过滤标签 "SpiderDebug"。
 * </p>
 *
 * <h3>使用方法：</h3>
 * <pre>
 * // 输出普通日志
 * SpiderDebug.log("开始请求 API");
 *
 * // 输出异常信息
 * try {
 *     // 你的代码
 * } catch (Exception e) {
 *     SpiderDebug.log(e);
 * }
 * </pre>
 *
 * <h3>查看日志：</h3>
 * <pre>
 * # Android Studio Logcat
 * 在过滤框输入: SpiderDebug
 *
 * # 命令行
 * adb logcat -s SpiderDebug:D
 * </pre>
 *
 * @author CatVod
 * @see Spider
 */
public class SpiderDebug {

    /**
     * 日志标签
     * <p>
     * 用于在 Logcat 中过滤日志，固定为 "SpiderDebug"。
     * </p>
     */
    private static final String TAG = SpiderDebug.class.getSimpleName();

    /**
     * 输出异常日志
     * <p>
     * 将异常信息输出到 Logcat，包含异常消息。
     * </p>
     *
     * @param e 异常对象
     *
     * <h4>示例：</h4>
     * <pre>
     * try {
     *     String json = OkHttp.string(url);
     * } catch (Exception e) {
     *     SpiderDebug.log(e);  // 输出异常信息
     * }
     * </pre>
     */
    public static void log(Throwable e) {
        Log.d(TAG, e.getMessage());
    }

    /**
     * 输出普通日志
     * <p>
     * 将普通消息输出到 Logcat。
     * </p>
     *
     * @param msg 日志消息
     *
     * <h4>示例：</h4>
     * <pre>
     * SpiderDebug.log("开始请求: " + url);
     * SpiderDebug.log("获取到 " + list.size() + " 条数据");
     * </pre>
     */
    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    /**
     * 输出脱敏后的日志（用于包含敏感信息的数据）
     * <p>
     * 自动检测并脱敏以下敏感信息：
     * <ul>
     *   <li>Token（保留前4位和后4位，中间用 *** 替换）</li>
     *   <li>Cookie（同上）</li>
     *   <li>Authorization（同上）</li>
     *   <li>Password（完全隐藏为 ******）</li>
     * </ul>
     * </p>
     *
     * @param msg 可能包含敏感信息的日志消息
     *
     * <h4>示例：</h4>
     * <pre>
     * String response = "{\"token\":\"abc123xyz789\",\"data\":{}}";
     * SpiderDebug.logSanitized(response);
     * // 输出: {"token":"abc1***x789","data":{}}
     * </pre>
     */
    public static void logSanitized(String msg) {
        if (msg == null) {
            Log.d(TAG, "null");
            return;
        }
        Log.d(TAG, sanitize(msg));
    }

    /**
     * 脱敏敏感信息
     * <p>
     * 使用正则表达式检测并脱敏常见的敏感字段。
     * </p>
     *
     * @param text 原始文本
     * @return 脱敏后的文本
     */
    private static String sanitize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 定义敏感字段的正则表达式模式
        // 匹配 JSON 格式: "token": "value" 或 token=value
        String[] sensitiveFields = {
                "token", "Token", "TOKEN",
                "cookie", "Cookie", "COOKIE",
                "authorization", "Authorization", "AUTHORIZATION",
                "password", "Password", "PASSWORD",
                "passwd", "Passwd", "PASSWD",
                "secret", "Secret", "SECRET",
                "apikey", "apiKey", "api_key", "API_KEY"
        };

        String result = text;

        for (String field : sensitiveFields) {
            // JSON 格式: "token": "abc123xyz789"
            Pattern jsonPattern = Pattern.compile(
                    "\"" + field + "\"\\s*:\\s*\"([^\"]{8,})\"",
                    Pattern.CASE_INSENSITIVE
            );
            result = maskSensitiveValue(result, jsonPattern);

            // URL 参数格式: token=abc123xyz789
            Pattern urlPattern = Pattern.compile(
                    field + "=([^&\\s\"']{8,})",
                    Pattern.CASE_INSENSITIVE
            );
            result = maskSensitiveValue(result, urlPattern);

            // Header 格式: Authorization: Bearer abc123xyz789
            Pattern headerPattern = Pattern.compile(
                    field + "\\s*:\\s*([^\\s\"']{8,})",
                    Pattern.CASE_INSENSITIVE
            );
            result = maskSensitiveValue(result, headerPattern);
        }

        return result;
    }

    /**
     * 对匹配的敏感值进行脱敏处理
     *
     * @param text 原始文本
     * @param pattern 匹配模式
     * @return 脱敏后的文本
     */
    private static String maskSensitiveValue(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String original = matcher.group(0);
            String value = matcher.group(1);

            // 脱敏策略：保留前4位和后4位，中间用 *** 替换
            String masked;
            if (value.length() <= 8) {
                // 值太短，完全隐藏
                masked = "******";
            } else {
                String prefix = value.substring(0, 4);
                String suffix = value.substring(value.length() - 4);
                masked = prefix + "***" + suffix;
            }

            // 替换原始值为脱敏值
            String replacement = original.replace(value, masked);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
