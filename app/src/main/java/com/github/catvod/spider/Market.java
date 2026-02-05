package com.github.catvod.spider;

import android.content.Context;
import android.net.Uri;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.market.Data;
import com.github.catvod.bean.market.Item;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.FileUtil;
import com.github.catvod.utils.Notify;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Util;
import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Response;

public class Market extends Spider {

    private static final String TAG = Market.class.getSimpleName();
    private List<Data> datas;

    @Override
    public void init(Context context, String extend) {
        if (extend.startsWith("http")) extend = OkHttp.string(extend);
        datas = Data.arrayFrom(extend);
    }

    @Override
    public String homeContent(boolean filter) {
        List<Class> classes = new ArrayList<>();
        if (datas.size() > 1) for (int i = 1; i < datas.size(); i++) classes.add(datas.get(i).type());
        return Result.string(classes, datas.get(0).getVod());
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        for (Data data : datas) if (data.getName().equals(tid)) return Result.get().page().vod(data.getVod()).string();
        return "";
    }

    /**
     * 执行下载操作
     * <p>
     * 使用 try-with-resources 确保 Response 资源正确释放。
     * 改进异常处理，记录具体错误信息。
     * </p>
     *
     * @param action 下载 URL
     * @return 结果通知
     */
    @Override
    public String action(String action) throws Exception {
        OkHttp.cancel(TAG);

        String name = Uri.parse(action).getLastPathSegment();
        if (name == null || name.isEmpty()) {
            Logger.w("Failed to extract filename from URL: " + action);
            return Result.notify("無效的下載地址");
        }

        Notify.show("正在下載..." + name);

        // 使用 try-with-resources 自动关闭 Response
        try (Response response = OkHttp.newCall(action, TAG)) {
            // 检查 response.body() 是否为 null
            if (response.body() == null) {
                Logger.e("Empty response body from: " + action);
                return Result.notify("下載失敗：空響應");
            }

            // 创建下载文件
            File file = Path.create(new File(Path.download(), name));

            // 下载文件
            download(file, response.body().byteStream());

            // 处理下载后的文件
            if (file.getName().endsWith(".zip")) {
                FileUtil.unzip(file, Path.download());
            }
            if (file.getName().endsWith(".apk")) {
                FileUtil.openFile(file);
            }

            // 检查并复制相关信息
            checkCopy(action);

            Logger.i("Download completed: " + name);
            return Result.notify("下載完成");

        } catch (IOException e) {
            Logger.e("Network error during download: " + action, e);
            return Result.notify("下載失敗：網絡錯誤");
        } catch (IllegalArgumentException e) {
            Logger.e("Invalid URL: " + action, e);
            return Result.notify("下載失敗：無效地址");
        }
    }

    private void download(File file, InputStream is) throws IOException {
        try (BufferedInputStream input = new BufferedInputStream(is); FileOutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[16384];
            int readBytes;
            while ((readBytes = input.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        }
    }

    private void checkCopy(String url) {
        for (Data data : datas) {
            int index = data.getList().indexOf(new Item(url));
            if (index == -1) continue;
            String text = data.getList().get(index).getCopy();
            if (!text.isEmpty()) Util.copy(text);
            break;
        }
    }

    @Override
    public void destroy() {
        OkHttp.cancel(TAG);
    }
}
