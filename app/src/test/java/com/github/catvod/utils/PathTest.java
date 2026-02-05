package com.github.catvod.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Path 文件操作工具类单元测试
 *
 * @author CatVod Team
 */
public class PathTest {

    private File testDir;

    @Before
    public void setUp() {
        // 创建临时测试目录
        testDir = new File(System.getProperty("java.io.tmpdir"), "catvod_test_" + System.currentTimeMillis());
        testDir.mkdirs();
    }

    @After
    public void tearDown() {
        // 清理测试目录
        if (testDir != null && testDir.exists()) {
            Path.clear(testDir);
        }
    }

    @Test
    public void testWrite_basic() {
        File file = new File(testDir, "test.txt");
        byte[] data = "Hello, World!".getBytes();

        File result = Path.write(file, data);

        assertTrue("文件应该被创建", result.exists());
        assertEquals("文件路径应该正确", file.getAbsolutePath(), result.getAbsolutePath());
    }

    @Test
    public void testRead_basic() {
        File file = new File(testDir, "test.txt");
        byte[] data = "Test Content".getBytes();
        Path.write(file, data);

        String content = Path.read(file);

        assertEquals("读取的内容应该正确", "Test Content", content);
    }

    @Test
    public void testWrite_overwrite() {
        File file = new File(testDir, "test.txt");

        // 第一次写入
        Path.write(file, "First".getBytes());
        assertEquals("第一次写入应该正确", "First", Path.read(file));

        // 第二次写入（覆盖）
        Path.write(file, "Second".getBytes());
        assertEquals("第二次写入应该覆盖", "Second", Path.read(file));
    }

    @Test
    public void testRead_nonExistentFile() {
        File file = new File(testDir, "non_existent.txt");

        String content = Path.read(file);

        assertEquals("不存在的文件应该返回空字符串", "", content);
    }

    @Test
    public void testCopy_file() throws IOException {
        File source = new File(testDir, "source.txt");
        File dest = new File(testDir, "dest.txt");

        Path.write(source, "Copy Test".getBytes());

        Path.copy(source, dest);

        assertTrue("目标文件应该被创建", dest.exists());
        assertEquals("目标文件内容应该正确", "Copy Test", Path.read(dest));
        assertTrue("源文件应该仍然存在", source.exists());
    }

    @Test
    public void testMove_file() {
        File source = new File(testDir, "source.txt");
        File dest = new File(testDir, "dest.txt");

        Path.write(source, "Move Test".getBytes());

        Path.move(source, dest);

        assertTrue("目标文件应该存在", dest.exists());
        assertFalse("源文件应该被删除", source.exists());
        assertEquals("目标文件内容应该正确", "Move Test", Path.read(dest));
    }

    @Test
    public void testClear_file() {
        File file = new File(testDir, "to_delete.txt");
        Path.write(file, "Delete me".getBytes());

        assertTrue("文件应该存在", file.exists());

        Path.clear(file);

        assertFalse("文件应该被删除", file.exists());
    }

    @Test
    public void testClear_directory() {
        // 创建目录结构
        File subDir = new File(testDir, "subdir");
        subDir.mkdirs();
        File file1 = new File(testDir, "file1.txt");
        File file2 = new File(subDir, "file2.txt");

        Path.write(file1, "File 1".getBytes());
        Path.write(file2, "File 2".getBytes());

        assertTrue("子目录应该存在", subDir.exists());
        assertTrue("文件应该存在", file1.exists());
        assertTrue("子目录中的文件应该存在", file2.exists());

        Path.clear(testDir);

        assertFalse("目录应该被删除", testDir.exists());
    }

    @Test
    public void testList_emptyDirectory() {
        File emptyDir = new File(testDir, "empty");
        emptyDir.mkdirs();

        java.util.List<File> files = Path.list(emptyDir);

        assertNotNull("列表不应为 null", files);
        assertEquals("空目录应该返回空列表", 0, files.size());
    }

    @Test
    public void testList_withFiles() {
        File file1 = new File(testDir, "a.txt");
        File file2 = new File(testDir, "b.txt");
        File subDir = new File(testDir, "subdir");

        Path.write(file1, "A".getBytes());
        Path.write(file2, "B".getBytes());
        subDir.mkdirs();

        java.util.List<File> files = Path.list(testDir);

        assertEquals("应该有 3 个条目", 3, files.size());

        // 验证排序（目录在前，文件在后，按名称排序）
        assertEquals("第一个应该是目录", "subdir", files.get(0).getName());
        assertEquals("第二个应该是 a.txt", "a.txt", files.get(1).getName());
        assertEquals("第三个应该是 b.txt", "b.txt", files.get(2).getName());
    }

    @Test
    public void testList_nullDirectory() {
        java.util.List<File> files = Path.list(null);

        assertNotNull("null 目录应该返回空列表", files);
        assertEquals("null 目录应该返回空列表", 0, files.size());
    }

    @Test
    public void testWrite_emptyData() {
        File file = new File(testDir, "empty.txt");
        Path.write(file, new byte[0]);

        assertTrue("空文件应该被创建", file.exists());
        assertEquals("文件大小应该为 0", 0, file.length());
    }

    @Test
    public void testRead_utf8() {
        File file = new File(testDir, "utf8.txt");
        String chineseText = "你好世界！测试中文字符。";

        Path.write(file, chineseText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        String content = Path.read(file);

        assertEquals("UTF-8 中文应该正确读取", chineseText, content);
    }
}
