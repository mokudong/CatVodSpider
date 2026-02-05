package com.github.catvod.api.contract;

import android.content.Context;

/**
 * 存储接口契约
 * <p>
 * 定义数据存储的标准接口，支持安全存储（加密）和普通存储。
 * 使得爬虫可以不依赖具体的存储实现（SharedPreferences、加密存储等）。
 * </p>
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *   <li>Cookie/Token 存储</li>
 *   <li>用户配置存储</li>
 *   <li>缓存数据存储</li>
 * </ul>
 * </p>
 *
 * @author CatVod Team
 * @since 2.0.0
 */
public interface IStorage {

    /**
     * 初始化存储
     *
     * @param context Android Context
     */
    void init(Context context);

    /**
     * 保存字符串
     *
     * @param key   键
     * @param value 值
     */
    void putString(String key, String value);

    /**
     * 获取字符串
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，不存在时返回默认值
     */
    String getString(String key, String defaultValue);

    /**
     * 保存整数
     */
    void putInt(String key, int value);

    /**
     * 获取整数
     */
    int getInt(String key, int defaultValue);

    /**
     * 保存布尔值
     */
    void putBoolean(String key, boolean value);

    /**
     * 获取布尔值
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * 删除键值对
     *
     * @param key 键
     */
    void remove(String key);

    /**
     * 清空所有数据
     */
    void clear();

    /**
     * 是否包含键
     *
     * @param key 键
     * @return 存在返回 true
     */
    boolean contains(String key);
}
