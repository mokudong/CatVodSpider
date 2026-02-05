package com.github.catvod.spider;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Sub;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.Image;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Local extends Spider {

    private SimpleDateFormat format;
    private List<File> allowedRoots;

    @Override
    public void init(Context context, String extend) {
        format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        initAllowedRoots();
    }

    /**
     * 初始化允许访问的根目录列表
     * <p>
     * 仅允许访问：
     * <ul>
     *   <li>外部存储根目录（/storage/emulated/0）</li>
     *   <li>/storage 下的其他挂载点（SD卡、USB等）</li>
     * </ul>
     * </p>
     */
    private void initAllowedRoots() {
        allowedRoots = new ArrayList<>();

        // 添加外部存储根目录
        allowedRoots.add(Environment.getExternalStorageDirectory());

        // 添加 /storage 下的所有挂载点
        File storageRoot = new File("/storage");
        File[] storageDirs = storageRoot.listFiles();
        if (storageDirs != null) {
            List<String> exclude = Arrays.asList("emulated", "self");
            for (File dir : storageDirs) {
                if (dir.isDirectory() && !exclude.contains(dir.getName())) {
                    allowedRoots.add(dir);
                }
            }
        }
    }

    /**
     * 验证文件路径是否安全（防止路径遍历攻击）
     * <p>
     * 检查目标文件的规范路径是否在允许的根目录内。
     * </p>
     *
     * @param path 用户提供的文件路径
     * @return 验证通过返回 File 对象，验证失败返回 null
     */
    private File validatePath(String path) {
        if (TextUtils.isEmpty(path)) {
            com.orhanobut.logger.Logger.w("Invalid path: empty or null");
            return null;
        }

        try {
            File file = new File(path);
            String canonicalPath = file.getCanonicalPath();

            // 检查是否在允许的根目录内
            for (File root : allowedRoots) {
                String rootCanonical = root.getCanonicalPath();
                if (canonicalPath.startsWith(rootCanonical)) {
                    return file;
                }
            }

            // 路径不在允许的根目录内
            com.orhanobut.logger.Logger.w("Path traversal attempt blocked: " + path + " -> " + canonicalPath);
            return null;
        } catch (Exception e) {
            com.orhanobut.logger.Logger.e("Path validation failed for: " + path, e);
            return null;
        }
    }

    @Override
    public String homeContent(boolean filter) {
        List<Class> classes = new ArrayList<>();
        classes.add(new Class(Environment.getExternalStorageDirectory().getAbsolutePath(), "本地文件", "1"));
        File[] files = new File("/storage").listFiles();
        if (files == null) return Result.string(classes);
        List<String> exclude = Arrays.asList("emulated", "sdcard", "self");
        for (File file : files) {
            if (exclude.contains(file.getName())) continue;
            classes.add(new Class(file.getAbsolutePath(), file.getName(), "1"));
        }
        return Result.string(classes);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        List<Vod> items = new ArrayList<>();
        List<File> files = Path.list(new File(tid));
        for (File file : files) {
            if (file.getName().startsWith(".")) continue;
            if (file.isDirectory() || Util.isMedia(file.getName())) items.add(create(file));
        }
        return Result.get().vod(items).page().string();
    }

    @Override
    public String detailContent(List<String> ids) {
        String url = ids.get(0);
        if (url.startsWith("http")) {
            String name = Uri.parse(url).getLastPathSegment();
            return Result.string(create(name, url));
        } else {
            // 路径遍历防护：验证文件路径
            File file = validatePath(ids.get(0));
            if (file == null) {
                // 路径验证失败，返回空结果
                com.orhanobut.logger.Logger.e("Access denied: invalid or unsafe path: " + ids.get(0));
                return Result.string(new ArrayList<>());
            }

            File parent = file.getParentFile();
            // 验证父目录路径
            if (parent != null) {
                parent = validatePath(parent.getAbsolutePath());
                if (parent == null) {
                    return Result.string(new ArrayList<>());
                }
            }

            List<File> files = Path.list(parent);
            return Result.string(create(parent != null ? parent : file, files));
        }
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        if (id.startsWith("http")) {
            return Result.get().url(id).string();
        } else {
            // 路径遍历防护：验证文件路径
            File validatedFile = validatePath(id);
            if (validatedFile == null) {
                com.orhanobut.logger.Logger.e("Access denied: invalid or unsafe path: " + id);
                // 返回空 URL（播放失败）
                return Result.get().url("").string();
            }

            // 使用验证后的规范路径
            String safePath = validatedFile.getAbsolutePath();
            return Result.get().url("file://" + safePath).subs(getSubs(safePath)).string();
        }
    }

    @Override
    public Object[] proxy(Map<String, String> params) {
        String path = new String(Base64.decode(params.get("path"), Base64.DEFAULT | Base64.URL_SAFE));
        Object[] result = new Object[3];
        result[0] = 200;
        result[1] = "application/octet-stream";
        result[2] = new ByteArrayInputStream(getBase64(path));
        return result;
    }

    private Vod create(String name, String url) {
        Vod vod = new Vod();
        vod.setTypeName("FongMi");
        vod.setVodId(url);
        vod.setVodName(name);
        vod.setVodPic(Image.VIDEO);
        vod.setVodPlayFrom("播放");
        vod.setVodPlayUrl(1 + "$" + url);
        return vod;
    }

    private Vod create(File file, List<File> files) {
        Vod vod = new Vod();
        vod.setTypeName("FongMi");
        vod.setVodId(file.getName());
        vod.setVodName(file.getName());
        vod.setVodPic(Image.VIDEO);
        vod.setVodPlayFrom("播放");
        List<String> playUrls = new ArrayList<>();
        for (File f : files) if (Util.isMedia(f.getName())) playUrls.add(f.getName() + "$" + f.getAbsolutePath());
        vod.setVodPlayUrl(TextUtils.join("#", playUrls));
        return vod;
    }

    private Vod create(File file) {
        Vod vod = new Vod();
        vod.setVodId(file.getAbsolutePath());
        vod.setVodName(file.getName());
        vod.setVodPic(file.isFile() ? Proxy.getUrl(siteKey, "&path=" + Base64.encodeToString(file.getAbsolutePath().getBytes(), Base64.DEFAULT | Base64.URL_SAFE)) : Image.FOLDER);
        vod.setVodRemarks(format.format(file.lastModified()));
        vod.setVodTag(file.isDirectory() ? "folder" : "file");
        return vod;
    }

    private byte[] getBase64(String path) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
        if (bitmap == null) {
            // 安全地解析默认图片（防止 ArrayIndexOutOfBoundsException）
            String[] parts = Image.VIDEO.split("base64,");
            if (parts.length < 2) {
                com.orhanobut.logger.Logger.e("Invalid base64 image format: " + Image.VIDEO);
                return new byte[0];
            }
            return Base64.decode(parts[1], Base64.DEFAULT);
        }

        // 使用 try-with-resources 确保资源释放
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            com.orhanobut.logger.Logger.e("Failed to compress bitmap for path: " + path, e);
            return new byte[0];
        } finally {
            // 释放 Bitmap 内存
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private List<Sub> getSubs(String path) {
        List<Sub> subs = new ArrayList<>();

        // 验证路径
        File validatedFile = validatePath(path);
        if (validatedFile == null) {
            return subs; // 返回空字幕列表
        }

        File parent = validatedFile.getParentFile();
        if (parent == null) {
            return subs;
        }

        for (File f : Path.list(parent)) {
            String ext = Util.getExt(f.getName());
            if (Util.isSub(ext)) {
                subs.add(Sub.create().name(Util.removeExt(f.getName())).ext(ext).url("file://" + f.getAbsolutePath()));
            }
        }
        return subs;
    }
}