package com.github.catvod.spider;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Filter;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Sub;
import com.github.catvod.bean.Vod;
import com.github.catvod.bean.alist.Drive;
import com.github.catvod.bean.alist.Item;
import com.github.catvod.bean.alist.Sorter;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.JsonValidator;
import com.github.catvod.utils.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AList extends Spider {

    /**
     * Filter 缓存
     * <p>
     * 使用静态 final 缓存 Filter 对象，避免重复创建。
     * Filter 是不可变对象，可以安全地在多个实例间共享。
     * </p>
     */
    private static final List<Filter> FILTER_CACHE = Collections.unmodifiableList(
            Arrays.asList(
                    new Filter("type", "排序類型", Arrays.asList(
                            new Filter.Value("預設", ""),
                            new Filter.Value("名稱", "name"),
                            new Filter.Value("大小", "size"),
                            new Filter.Value("修改時間", "date")
                    )),
                    new Filter("order", "排序方式", Arrays.asList(
                            new Filter.Value("預設", ""),
                            new Filter.Value("⬆", "asc"),
                            new Filter.Value("⬇", "desc")
                    ))
            )
    );

    private ExecutorService executor;
    private List<Drive> drives;
    private String ext;

    /**
     * 获取筛选条件
     * <p>
     * 返回缓存的 Filter 对象，避免重复创建。
     * </p>
     *
     * @return Filter 列表
     */
    private List<Filter> getFilter() {
        return FILTER_CACHE;
    }

    private void fetchRule() {
        if (drives != null && !drives.isEmpty()) return;
        if (ext.startsWith("http")) ext = OkHttp.string(ext);
        drives = Drive.arrayFrom(ext);
    }

    private Drive getDrive(String name) {
        if (drives == null || drives.isEmpty()) {
            throw new IllegalStateException("Drives not initialized. Call init() first or check ext configuration.");
        }

        int index = drives.indexOf(new Drive(name));
        if (index == -1) {
            throw new IllegalArgumentException("Drive not found: " + name + ". Available drives: " + getDriveNames());
        }

        return drives.get(index).check();
    }

    private String getDriveNames() {
        if (drives == null || drives.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < drives.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(drives.get(i).getName());
        }
        sb.append("]");
        return sb.toString();
    }

    private String post(Drive drive, String url, String param) {
        return post(drive, url, param, true);
    }

    private String post(Drive drive, String url, String param, boolean retry) {
        String response = OkHttp.post(url, param, drive.getHeader()).getBody();
        // 使用脱敏日志输出（响应可能包含 token）
        SpiderDebug.logSanitized(response);
        if (retry && response.contains("Guest user is disabled") && login(drive)) return post(drive, url, param, false);
        return response;
    }

    @Override
    public void init(Context context, String extend) {
        executor = Executors.newCachedThreadPool();
        ext = extend;
        fetchRule();
    }

    @Override
    public String homeContent(boolean filter) {
        List<Class> classes = new ArrayList<>();
        LinkedHashMap<String, List<Filter>> filters = new LinkedHashMap<>();
        for (Drive drive : drives) if (!drive.hidden()) classes.add(drive.toType());
        for (Class item : classes) filters.put(item.getTypeId(), getFilter());
        return Result.string(classes, filters);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        String type = extend.containsKey("type") ? extend.get("type") : "";
        String order = extend.containsKey("order") ? extend.get("order") : "";
        List<Item> folders = new ArrayList<>();
        List<Item> files = new ArrayList<>();
        List<Vod> list = new ArrayList<>();

        for (Item item : getList(tid, true)) {
            if (item.isFolder()) folders.add(item);
            else files.add(item);
        }
        if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(order)) {
            Sorter.sort(type, order, folders);
            Sorter.sort(type, order, files);
        }

        for (Item item : folders) list.add(item.getVod(tid));
        for (Item item : files) list.add(item.getVod(tid));
        return Result.get().vod(list).page().string();
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        // 空指针防护
        if (ids == null || ids.isEmpty()) {
            Logger.w("detailContent called with null or empty ids");
            return Result.string(new ArrayList<>());
        }

        String id = ids.get(0);
        if (id == null || id.isEmpty()) {
            Logger.w("detailContent called with null or empty id");
            return Result.string(new ArrayList<>());
        }

        String key = id.contains("/") ? id.substring(0, id.indexOf("/")) : id;
        String path = id.substring(0, id.lastIndexOf("/"));
        String name = path.substring(path.lastIndexOf("/") + 1);
        Drive drive = getDrive(key);
        Vod vod = new Vod();
        vod.setVodPlayFrom(key);
        vod.setVodId(id);
        vod.setVodName(name);
        List<String> playUrls = new ArrayList<>();
        List<Item> parents = getList(path, false);
        for (Item item : parents) if (item.isMedia(drive.isNew())) playUrls.add(item.getName() + "$" + encodeVodId(item.getVodId(path) + findSubs(path, parents)));
        vod.setVodPlayUrl(TextUtils.join("#", playUrls));
        return Result.string(vod);
    }

    private String encodeVodId(String vodId) {
        if (vodId.contains("#")) return vodId.replace("#", "***");
        return vodId;
    }

    private String decodeVodId(String vodId) {
        if (vodId.contains("***")) return vodId.replace("***", "#");
        return vodId;
    }

    @Override
    public String searchContent(String keyword, boolean quick) throws Exception {
        List<Vod> list = new ArrayList<>();
        List<Job> jobs = new ArrayList<>();
        for (Drive drive : drives) if (drive.search()) jobs.add(new Job(drive.check(), keyword));
        for (Future<List<Vod>> future : executor.invokeAll(jobs, 15, TimeUnit.SECONDS)) list.addAll(future.get());
        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        String[] ids = decodeVodId(id).split("~~~");
        String url = getDetail(ids[0]).getUrl();
        return Result.get().url(url).header(getPlayHeader(url)).subs(getSubs(ids)).string();
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }

    private static Map<String, String> getPlayHeader(String url) {
        try {
            Uri uri = Uri.parse(url);
            Map<String, String> header = new HashMap<>();
            if (uri.getHost().contains("115")) header.put("User-Agent", Util.CHROME);
            if (uri.getHost().contains("baidupcs.com")) header.put("User-Agent", "pan.baidu.com");
            return header;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 登录 AList 获取 Token
     * <p>
     * 使用 JsonValidator 验证响应格式，防止数据格式错误。
     * </p>
     *
     * @param drive 驱动配置
     * @return 登录成功返回 true，失败返回 false
     */
    private boolean login(Drive drive) {
        try {
            JSONObject params = new JSONObject();
            params.put("username", drive.getLogin().getUsername());
            params.put("password", drive.getLogin().getPassword());

            String response = OkHttp.post(drive.loginApi(), params.toString());

            // 使用 JsonValidator 验证响应格式
            JsonObject jsonObj = JsonValidator.validateResponse(response, "object");

            // 安全提取 token
            JsonObject data = JsonValidator.safeGetJsonObject(jsonObj, "data");
            if (data == null) {
                Logger.w("Login response missing 'data' field");
                return false;
            }

            String token = JsonValidator.safeGetString(data, "token", null);
            if (token == null || token.isEmpty()) {
                Logger.w("Login response missing or empty 'token' field");
                return false;
            }

            drive.setToken(token);
            Logger.i("AList login successful for drive: " + drive.getName());
            return true;

        } catch (JsonValidator.ValidationException e) {
            Logger.e("Login response validation failed", e);
            return false;
        } catch (JSONException e) {
            Logger.e("Failed to create login request params", e);
            return false;
        }
    }

    /**
     * 获取文件/目录详情
     * <p>
     * 改进异常处理，记录具体错误信息。
     * </p>
     *
     * @param id 文件/目录 ID
     * @return Item 对象，失败返回空 Item
     */
    private Item getDetail(String id) {
        try {
            String key = id.contains("/") ? id.substring(0, id.indexOf("/")) : id;
            String path = id.contains("/") ? id.substring(id.indexOf("/")) : "";
            Drive drive = getDrive(key);
            path = path.startsWith(drive.getPath()) ? path : drive.getPath() + path;

            JSONObject params = new JSONObject();
            params.put("path", path);
            params.put("password", drive.findPass(path));

            String response = post(drive, drive.getApi(), params.toString());
            return Item.objectFrom(getDetailJson(drive.isNew(), response));

        } catch (IllegalArgumentException e) {
            Logger.e("Invalid drive or path: " + id, e);
            return new Item();
        } catch (JSONException e) {
            Logger.e("Failed to parse detail response for: " + id, e);
            return new Item();
        } catch (JsonValidator.ValidationException e) {
            Logger.e("Detail response validation failed for: " + id, e);
            return new Item();
        }
    }

    /**
     * 获取文件/目录列表
     * <p>
     * 改进异常处理，记录具体错误信息。
     * </p>
     *
     * @param id     目录 ID
     * @param filter 是否过滤
     * @return Item 列表，失败返回空列表
     */
    private List<Item> getList(String id, boolean filter) {
        try {
            String key = id.contains("/") ? id.substring(0, id.indexOf("/")) : id;
            String path = id.contains("/") ? id.substring(id.indexOf("/")) : "";
            Drive drive = getDrive(key);
            path = path.startsWith(drive.getPath()) ? path : drive.getPath() + path;

            JSONObject params = new JSONObject();
            params.put("path", path);
            params.put("password", drive.findPass(path));

            String response = post(drive, drive.listApi(), params.toString());
            List<Item> items = Item.arrayFrom(getListJson(drive.isNew(), response));

            // 过滤
            if (filter) {
                Iterator<Item> iterator = items.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().ignore(drive.isNew())) {
                        iterator.remove();
                    }
                }
            }

            return items;

        } catch (IllegalArgumentException e) {
            Logger.e("Invalid drive or path: " + id, e);
            return Collections.emptyList();
        } catch (JSONException e) {
            Logger.e("Failed to parse list response for: " + id, e);
            return Collections.emptyList();
        } catch (JsonValidator.ValidationException e) {
            Logger.e("List response validation failed for: " + id, e);
            return Collections.emptyList();
        }
    }

    /**
     * 提取列表 JSON
     * <p>
     * 使用 JsonValidator 验证响应格式，支持新旧版本 AList API。
     * </p>
     *
     * @param isNew    是否为新版 API
     * @param response 响应字符串
     * @return 列表 JSON 字符串
     * @throws JSONException         JSON 解析异常
     * @throws JsonValidator.ValidationException 响应验证异常
     */
    private String getListJson(boolean isNew, String response) throws JSONException, JsonValidator.ValidationException {
        // 使用 JsonValidator 验证响应
        JsonObject jsonObj = JsonValidator.validateResponse(response, "object");

        if (isNew) {
            // 新版 API: data.content[]
            JsonObject data = JsonValidator.safeGetJsonObject(jsonObj, "data");
            if (data == null) {
                throw new JSONException("Missing 'data' field in response");
            }
            JsonArray content = data.has("content") ? data.getAsJsonArray("content") : new JsonArray();
            return content.toString();
        } else {
            // 旧版 API: data.files[]
            JsonObject data = JsonValidator.safeGetJsonObject(jsonObj, "data");
            if (data == null) {
                throw new JSONException("Missing 'data' field in response");
            }
            JsonArray files = data.has("files") ? data.getAsJsonArray("files") : new JsonArray();
            return files.toString();
        }
    }

    /**
     * 提取详情 JSON
     * <p>
     * 使用 JsonValidator 验证响应格式，支持新旧版本 AList API。
     * </p>
     *
     * @param isNew    是否为新版 API
     * @param response 响应字符串
     * @return 详情 JSON 字符串
     * @throws JSONException         JSON 解析异常
     * @throws JsonValidator.ValidationException 响应验证异常
     */
    private String getDetailJson(boolean isNew, String response) throws JSONException, JsonValidator.ValidationException {
        // 使用 JsonValidator 验证响应
        JsonObject jsonObj = JsonValidator.validateResponse(response, "object");

        if (isNew) {
            // 新版 API: data{}
            JsonObject data = JsonValidator.safeGetJsonObject(jsonObj, "data");
            if (data == null) {
                throw new JSONException("Missing 'data' field in response");
            }
            return data.toString();
        } else {
            // 旧版 API: data.files[0]
            JsonObject data = JsonValidator.safeGetJsonObject(jsonObj, "data");
            if (data == null) {
                throw new JSONException("Missing 'data' field in response");
            }
            JsonArray files = data.has("files") ? data.getAsJsonArray("files") : null;
            if (files == null || files.size() == 0) {
                throw new JSONException("No files found in response");
            }
            return files.get(0).getAsJsonObject().toString();
        }
    }

    /**
     * 提取搜索 JSON
     * <p>
     * 使用 JsonValidator 验证响应格式，支持新旧版本 AList API。
     * </p>
     *
     * @param isNew    是否为新版 API
     * @param response 响应字符串
     * @return 搜索结果 JSON 字符串
     * @throws JSONException         JSON 解析异常
     * @throws JsonValidator.ValidationException 响应验证异常
     */
    private String getSearchJson(boolean isNew, String response) throws JSONException, JsonValidator.ValidationException {
        // 使用 JsonValidator 验证响应
        JsonObject jsonObj = JsonValidator.validateResponse(response, "object");

        if (isNew) {
            // 新版 API: data.content[]
            JsonObject data = JsonValidator.safeGetJsonObject(jsonObj, "data");
            if (data == null) {
                throw new JSONException("Missing 'data' field in response");
            }
            JsonArray content = data.has("content") ? data.getAsJsonArray("content") : new JsonArray();
            return content.toString();
        } else {
            // 旧版 API: data[]
            JsonArray data = jsonObj.has("data") ? jsonObj.getAsJsonArray("data") : new JsonArray();
            return data.toString();
        }
    }

    private String findSubs(String path, List<Item> items) {
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            String ext = Util.getExt(item.getName());
            if (Util.isSub(ext)) sb.append("~~~").append(item.getName()).append("@@@").append(ext).append("@@@").append(item.getVodId(path));
        }
        return sb.toString();
    }

    private List<Sub> getSubs(String[] ids) {
        List<Sub> sub = new ArrayList<>();
        for (String text : ids) {
            if (!text.contains("@@@")) continue;
            String[] split = text.split("@@@");
            String name = split[0];
            String ext = split[1];
            String url = getDetail(split[2]).getUrl();
            sub.add(Sub.create().name(name).ext(ext).url(url));
        }
        return sub;
    }

    class Job implements Callable<List<Vod>> {

        private final Drive drive;
        private final String keyword;

        public Job(Drive drive, String keyword) {
            this.drive = drive;
            this.keyword = keyword;
        }

        @Override
        public List<Vod> call() {
            try {
                List<Vod> list = new ArrayList<>();
                String response = post(drive, drive.searchApi(), drive.params(keyword));
                List<Item> items = Item.arrayFrom(getSearchJson(drive.isNew(), response));
                for (Item item : items) if (!item.ignore(drive.isNew())) list.add(item.getVod(drive));
                return list;
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
    }
}