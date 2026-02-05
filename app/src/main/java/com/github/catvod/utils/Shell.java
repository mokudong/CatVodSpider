package com.github.catvod.utils;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Shell 命令执行工具类
 * <p>
 * <b>⚠️ 安全提示：</b>
 * 此类使用 ProcessBuilder 执行 shell 命令，自动转义参数以防止命令注入攻击。
 * 不要直接使用 Runtime.exec(String command)，因为它会将整个字符串传递给 shell 解析，
 * 可能导致命令注入漏洞。
 * </p>
 *
 * @author CatVod
 * @version 2.0 (安全版本)
 */
public class Shell {

    /**
     * 执行 shell 命令（参数数组形式，安全）
     * <p>
     * 使用 ProcessBuilder 执行命令，参数会被自动转义，防止命令注入。
     * </p>
     *
     * <h3>使用示例：</h3>
     * <pre>
     * // ✓ 正确用法：参数分离
     * Shell.exec("chmod", "755", "/path/to/file");
     *
     * // ✓ 包含特殊字符的文件名也安全
     * Shell.exec("chmod", "755", "/path/file; rm -rf /");  // 不会执行删除命令
     *
     * // ❌ 错误用法：不要拼接字符串
     * Shell.exec("chmod 755 " + filename);  // 已弃用，存在注入风险
     * </pre>
     *
     * @param command 命令及其参数，每个元素为一个独立参数
     * @throws IOException 命令执行失败时抛出
     * @throws InterruptedException 进程被中断时抛出
     */
    public static void exec(String... command) throws IOException, InterruptedException {
        if (command == null || command.length == 0) {
            throw new IllegalArgumentException("Command cannot be null or empty");
        }

        Logger.d("Executing command: " + Arrays.toString(command));

        // 使用 ProcessBuilder 防止命令注入
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);  // 合并标准输出和错误输出

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            // 读取错误输出
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }

            String errorMessage = "Command failed with exit code " + exitCode +
                    "\nCommand: " + Arrays.toString(command) +
                    "\nOutput: " + errorOutput.toString();

            Logger.e(errorMessage);
            throw new IOException(errorMessage);
        }

        Logger.d("Command executed successfully");
    }

    /**
     * 执行 shell 命令（字符串形式，已弃用）
     * <p>
     * <b>⚠️ 安全警告：此方法已弃用，存在命令注入风险！</b>
     * </p>
     * <p>
     * 请使用 {@link #exec(String...)} 替代。
     * </p>
     *
     * @param command 完整的命令字符串
     * @throws UnsupportedOperationException 此方法已被禁用
     * @deprecated 使用 {@link #exec(String...)} 替代
     */
    @Deprecated
    public static void exec(String command) {
        throw new UnsupportedOperationException(
                "Executing shell commands as strings is UNSAFE and has been disabled.\n" +
                        "Use Shell.exec(String... command) instead.\n" +
                        "Example: Shell.exec(\"chmod\", \"755\", filename)");
    }

    /**
     * 执行 shell 命令并返回输出
     *
     * @param command 命令及其参数
     * @return 命令的标准输出
     * @throws IOException 命令执行失败时抛出
     * @throws InterruptedException 进程被中断时抛出
     */
    public static String execWithOutput(String... command) throws IOException, InterruptedException {
        if (command == null || command.length == 0) {
            throw new IllegalArgumentException("Command cannot be null or empty");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with exit code " + exitCode +
                    "\nCommand: " + Arrays.toString(command) +
                    "\nOutput: " + output.toString());
        }

        return output.toString();
    }
}
