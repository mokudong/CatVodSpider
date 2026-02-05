package com.github.catvod.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.github.catvod.spider.Init;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class FileUtil {

    public static void openFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(getShareUri(file), FileUtil.getMimeType(file.getName()));
        Init.context().startActivity(intent);
    }

    /**
     * 解压 ZIP 文件
     * <p>
     * 改进异常处理，使用 Logger 记录错误。
     * </p>
     *
     * @param target ZIP 文件
     * @param path   解压目标目录
     */
    public static void unzip(File target, File path) {
        if (target == null || !target.exists()) {
            Logger.w("Target file does not exist: " + target);
            return;
        }

        if (path == null) {
            Logger.w("Target path is null");
            return;
        }

        try (ZipFile zip = new ZipFile(target.getAbsolutePath())) {
            Enumeration<?> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File out = new File(path, entry.getName());

                if (entry.isDirectory()) {
                    if (!out.mkdirs() && !out.exists()) {
                        Logger.w("Failed to create directory: " + out.getAbsolutePath());
                    }
                } else {
                    Path.copy(zip.getInputStream(entry), out);
                }
            }
            Logger.i("Unzip completed: " + target.getName() + " -> " + path.getAbsolutePath());

        } catch (ZipException e) {
            Logger.e("Invalid ZIP file: " + target.getAbsolutePath(), e);
        } catch (IOException e) {
            Logger.e("IO error while unzipping: " + target.getAbsolutePath(), e);
        } catch (SecurityException e) {
            Logger.e("Permission denied while unzipping: " + target.getAbsolutePath(), e);
        }
    }

    private static Uri getShareUri(File file) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N ? Uri.fromFile(file) : FileProvider.getUriForFile(Init.context(), Init.context().getPackageName() + ".provider", file);
    }

    private static String getMimeType(String fileName) {
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        return TextUtils.isEmpty(mimeType) ? "*/*" : mimeType;
    }
}
