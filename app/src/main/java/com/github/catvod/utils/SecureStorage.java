package com.github.catvod.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.orhanobut.logger.Logger;

/**
 * 安全存储工具类
 * <p>
 * 使用 AndroidX Security Crypto 库提供的 EncryptedSharedPreferences 实现加密存储。
 * 数据使用 AES256-GCM 加密，密钥存储在 Android Keystore 中（硬件级别安全）。
 * </p>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>密钥加密：使用 AES256-SIV 加密 SharedPreferences 的 key</li>
 *   <li>值加密：使用 AES256-GCM 加密 SharedPreferences 的 value</li>
 *   <li>硬件保护：主密钥存储在 Android Keystore（TEE/SE）</li>
 *   <li>自动密钥管理：密钥生成、轮换由系统自动管理</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 初始化（通常在 Application.onCreate() 中）
 * SecureStorage.init(context);
 *
 * // 保存敏感数据
 * SecureStorage.saveCookie("my_session_cookie");
 * SecureStorage.saveToken("access_token_here");
 *
 * // 读取敏感数据
 * String cookie = SecureStorage.getCookie();
 * String token = SecureStorage.getToken();
 *
 * // 清除敏感数据（退出登录时）
 * SecureStorage.clearAll();
 * </pre>
 *
 * @author CatVod
 * @version 1.0
 */
public class SecureStorage {

    /**
     * 加密 SharedPreferences 文件名
     */
    private static final String PREFS_NAME = "secure_credentials";

    /**
     * Cookie 存储的 key
     */
    private static final String KEY_COOKIE = "cookie";

    /**
     * Token 存储的 key
     */
    private static final String KEY_TOKEN = "token";

    /**
     * 用户名存储的 key
     */
    private static final String KEY_USERNAME = "username";

    /**
     * 加密的 SharedPreferences 实例
     */
    private static SharedPreferences encryptedPrefs;

    /**
     * 初始化安全存储
     * <p>
     * 必须在使用前调用，建议在 Application.onCreate() 中初始化。
     * </p>
     *
     * @param context Android 上下文
     * @throws SecurityException 初始化失败时抛出
     */
    public static void init(Context context) throws SecurityException {
        try {
            // 创建或获取主密钥（存储在 Android Keystore 中）
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // 创建加密的 SharedPreferences
            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            Logger.i("SecureStorage initialized successfully");

        } catch (Exception e) {
            String errorMessage = "Failed to initialize SecureStorage: " + e.getMessage();
            Logger.e(errorMessage, e);
            throw new SecurityException(errorMessage, e);
        }
    }

    /**
     * 保存 Cookie
     *
     * @param cookie Cookie字符串
     */
    public static void saveCookie(String cookie) {
        checkInitialized();
        encryptedPrefs.edit()
                .putString(KEY_COOKIE, cookie)
                .apply();
        Logger.d("Cookie saved securely");
    }

    /**
     * 获取 Cookie
     *
     * @return Cookie字符串，如果不存在返回空字符串
     */
    public static String getCookie() {
        checkInitialized();
        return encryptedPrefs.getString(KEY_COOKIE, "");
    }

    /**
     * 保存访问令牌
     *
     * @param token 访问令牌
     */
    public static void saveToken(String token) {
        checkInitialized();
        encryptedPrefs.edit()
                .putString(KEY_TOKEN, token)
                .apply();
        Logger.d("Token saved securely");
    }

    /**
     * 获取访问令牌
     *
     * @return 访问令牌，如果不存在返回空字符串
     */
    public static String getToken() {
        checkInitialized();
        return encryptedPrefs.getString(KEY_TOKEN, "");
    }

    /**
     * 保存用户名
     *
     * @param username 用户名
     */
    public static void saveUsername(String username) {
        checkInitialized();
        encryptedPrefs.edit()
                .putString(KEY_USERNAME, username)
                .apply();
        Logger.d("Username saved securely");
    }

    /**
     * 获取用户名
     *
     * @return 用户名，如果不存在返回空字符串
     */
    public static String getUsername() {
        checkInitialized();
        return encryptedPrefs.getString(KEY_USERNAME, "");
    }

    /**
     * 保存自定义键值对
     *
     * @param key 键名
     * @param value 值
     */
    public static void save(String key, String value) {
        checkInitialized();
        encryptedPrefs.edit()
                .putString(key, value)
                .apply();
        Logger.d("Data saved securely: key=" + key);
    }

    /**
     * 获取自定义键值对
     *
     * @param key 键名
     * @param defaultValue 默认值
     * @return 值或默认值
     */
    public static String get(String key, String defaultValue) {
        checkInitialized();
        return encryptedPrefs.getString(key, defaultValue);
    }

    /**
     * 删除指定键的数据
     *
     * @param key 键名
     */
    public static void remove(String key) {
        checkInitialized();
        encryptedPrefs.edit()
                .remove(key)
                .apply();
        Logger.d("Data removed: key=" + key);
    }

    /**
     * 清除 Cookie
     */
    public static void clearCookie() {
        checkInitialized();
        encryptedPrefs.edit()
                .remove(KEY_COOKIE)
                .apply();
        Logger.d("Cookie cleared");
    }

    /**
     * 清除 Token
     */
    public static void clearToken() {
        checkInitialized();
        encryptedPrefs.edit()
                .remove(KEY_TOKEN)
                .apply();
        Logger.d("Token cleared");
    }

    /**
     * 清除所有数据
     * <p>
     * 用于用户退出登录时清理所有敏感信息。
     * </p>
     */
    public static void clearAll() {
        checkInitialized();
        encryptedPrefs.edit()
                .clear()
                .apply();
        Logger.i("All secure data cleared");
    }

    /**
     * 检查是否已初始化
     *
     * @throws IllegalStateException 未初始化时抛出
     */
    private static void checkInitialized() {
        if (encryptedPrefs == null) {
            throw new IllegalStateException(
                    "SecureStorage not initialized. Call SecureStorage.init(context) first.");
        }
    }

    /**
     * 检查是否存在某个键
     *
     * @param key 键名
     * @return true=存在, false=不存在
     */
    public static boolean contains(String key) {
        checkInitialized();
        return encryptedPrefs.contains(key);
    }

    /**
     * 获取所有存储的键
     *
     * @return 键的集合
     */
    public static java.util.Set<String> getAllKeys() {
        checkInitialized();
        return encryptedPrefs.getAll().keySet();
    }
}
