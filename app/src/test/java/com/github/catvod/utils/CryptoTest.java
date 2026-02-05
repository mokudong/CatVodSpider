package com.github.catvod.utils;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Crypto 加密工具类单元测试
 * <p>
 * 测试 MD5、AES、RSA 加密和随机密钥生成。
 * </p>
 *
 * @author CatVod Team
 */
public class CryptoTest {

    @Test
    public void testMd5_basic() {
        String result = Crypto.md5("hello world");

        assertNotNull("MD5 结果不应为 null", result);
        assertEquals("MD5 长度应为 32", 32, result.length());
        assertEquals("MD5 值应该一致", "5eb63bbbe01eeed093cb22bb8f5acdc3", result);
    }

    @Test
    public void testMd5_emptyString() {
        String result = Crypto.md5("");

        assertNotNull("空字符串的 MD5 不应为 null", result);
        assertEquals("空字符串的 MD5 应该正确", "d41d8cd98f00b204e9800998ecf8427e", result);
    }

    @Test
    public void testMd5_consistency() {
        String input = "test data";
        String result1 = Crypto.md5(input);
        String result2 = Crypto.md5(input);

        assertEquals("相同输入应该产生相同的 MD5", result1, result2);
    }

    @Test
    public void testMd5_differentInput() {
        String hash1 = Crypto.md5("hello");
        String hash2 = Crypto.md5("world");

        assertNotEquals("不同输入应该产生不同的 MD5", hash1, hash2);
    }

    @Test
    public void testMd5_lowercase() {
        String result = Crypto.md5("TEST");

        // MD5 应该是小写
        assertEquals("MD5 应该是小写", result, result.toLowerCase());
    }

    @Test
    public void testAesEncryptDecrypt() throws Exception {
        String plaintext = "Hello, World!";
        String key = "1234567890123456";  // 16 bytes
        String iv = "abcdefghijklmnop";   // 16 bytes

        // 加密
        String encrypted = Crypto.aesEncrypt(plaintext, key, iv);
        assertNotNull("加密结果不应为 null", encrypted);
        assertNotEquals("密文应该与明文不同", plaintext, encrypted);

        // 解密
        String decrypted = Crypto.CBC(encrypted, key, iv);
        assertEquals("解密后应该恢复原始明文", plaintext, decrypted);
    }

    @Test
    public void testAesEncrypt_emptyString() throws Exception {
        String key = "1234567890123456";
        String iv = "abcdefghijklmnop";

        String encrypted = Crypto.aesEncrypt("", key, iv);
        assertNotNull("空字符串加密不应为 null", encrypted);

        String decrypted = Crypto.CBC(encrypted, key, iv);
        assertEquals("解密空字符串应该得到空字符串", "", decrypted);
    }

    @Test
    public void testRandomKey_length() {
        int size = 16;
        String key = Crypto.randomKey(size);

        assertNotNull("随机密钥不应为 null", key);
        assertEquals("密钥长度应该正确", size, key.length());
    }

    @Test
    public void testRandomKey_differentKeys() {
        String key1 = Crypto.randomKey(16);
        String key2 = Crypto.randomKey(16);

        assertNotEquals("多次生成应该产生不同的密钥", key1, key2);
    }

    @Test
    public void testRandomKey_onlyAlphanumeric() {
        String key = Crypto.randomKey(100);

        // 验证只包含字母和数字
        assertTrue("密钥应该只包含字母和数字", key.matches("[a-zA-Z0-9]+"));
    }

    @Test
    public void testRandomKey_distribution() {
        // 测试随机性：生成多个密钥，确保字符分布均匀
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            keys.add(Crypto.randomKey(16));
        }

        // 100 个密钥应该都不相同（随机性测试）
        assertEquals("100个密钥应该都不相同", 100, keys.size());
    }

    @Test
    public void testRandomKey_variousSizes() {
        for (int size : new int[]{1, 8, 16, 32, 64, 128}) {
            String key = Crypto.randomKey(size);
            assertEquals("密钥长度应该正确 (size=" + size + ")", size, key.length());
        }
    }

    @Test
    public void testMd5_withCharset() {
        // 测试不同字符集
        String chinese = "你好世界";
        String hash1 = Crypto.md5(chinese, "UTF-8");
        String hash2 = Crypto.md5(chinese, "UTF-8");

        assertNotNull("中文字符串的 MD5 不应为 null", hash1);
        assertEquals("相同字符串和字符集应该产生相同的 MD5", hash1, hash2);
    }

    @Test
    public void testAesEncrypt_longText() throws Exception {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("This is a long text. ");
        }

        String key = "1234567890123456";
        String iv = "abcdefghijklmnop";

        String encrypted = Crypto.aesEncrypt(longText.toString(), key, iv);
        String decrypted = Crypto.CBC(encrypted, key, iv);

        assertEquals("长文本加解密应该正确", longText.toString(), decrypted);
    }

    @Test
    public void testAesEncrypt_specialCharacters() throws Exception {
        String text = "特殊字符：!@#$%^&*()_+-=[]{}|;':\",./<>?\n\t";
        String key = "1234567890123456";
        String iv = "abcdefghijklmnop";

        String encrypted = Crypto.aesEncrypt(text, key, iv);
        String decrypted = Crypto.CBC(encrypted, key, iv);

        assertEquals("特殊字符加解密应该正确", text, decrypted);
    }
}
