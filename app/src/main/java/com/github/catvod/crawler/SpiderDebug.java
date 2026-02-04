package com.github.catvod.crawler;

import android.util.Log;

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
}
