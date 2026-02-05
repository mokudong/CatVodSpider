package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.bean.jianpian.Data;
import com.github.catvod.bean.jianpian.Detail;
import com.github.catvod.bean.jianpian.Resp;
import com.github.catvod.bean.jianpian.Search;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.JsonValidator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orhanobut.logger.Logger;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Qile
 */
public class Jianpian extends Spider {

    private String siteUrl = "https://ev5356.970xw.com";
    private String imgDomain;
    private String extend;

    private Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 9; V2196A Build/PQ3A.190705.08211809; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4472.114 Mobile Safari/537.36;webank/h5face;webank/1.0;netType:NETWORK_WIFI;appVersion:416;packageName:com.jp3.xg3");
        headers.put("Referer", siteUrl);
        return headers;
    }

    /**
     * 初始化 Jianpian 爬虫
     * <p>
     * 通过 DNS 解析获取实际域名，并获取图片域名配置。
     * 使用 JsonValidator 验证所有网络响应。
     * </p>
     *
     * @param context Android Context
     * @param extend  扩展参数
     * @throws Exception 初始化失败异常
     */
    @Override
    public void init(Context context, String extend) throws Exception {
        this.extend = extend;

        try {
            // 获取 DNS 解析结果
            String dnsResponse = OkHttp.string("https://dns.alidns.com/resolve?name=swrdsfeiujo25sw.cc&type=TXT");
            if (TextUtils.isEmpty(dnsResponse)) {
                Logger.w("DNS resolution returned empty response");
                return;
            }

            // 使用 JsonValidator 验证 DNS 响应
            JsonObject domains = JsonValidator.validateResponse(dnsResponse, "object");
            JsonArray answerArray = Json.safeGetJsonArray(domains, "Answer");

            if (answerArray.size() == 0) {
                Logger.w("DNS Answer array is empty");
                return;
            }

            // 安全获取 data 字段
            JsonObject firstAnswer = answerArray.get(0).getAsJsonObject();
            String parts = Json.safeGetString(firstAnswer, "data", "");
            if (TextUtils.isEmpty(parts)) {
                Logger.w("DNS data field is empty");
                return;
            }

            parts = parts.replace("\"", "");
            String[] domain = parts.split(",");

            // 遍历域名，找到可用的
            for (String d : domain) {
                siteUrl = "https://wangerniu." + d;
                Logger.d("Trying domain: " + siteUrl);

                String json = OkHttp.string(siteUrl + "/api/v2/settings/resourceDomainConfig");
                if (TextUtils.isEmpty(json)) {
                    Logger.d("Domain returned empty response, trying next");
                    continue;
                }

                try {
                    // 使用 JsonValidator 验证响应
                    JsonObject root = JsonValidator.validateResponse(json, "object");
                    JsonObject data = Json.safeGetJsonObject(root, "data");

                    String imgDomainStr = Json.safeGetString(data, "imgDomain", "");
                    if (!TextUtils.isEmpty(imgDomainStr)) {
                        String[] imgDomains = imgDomainStr.split(",");
                        if (imgDomains.length > 0) {
                            imgDomain = imgDomains[0];
                            Logger.i("Jianpian initialized successfully with domain: " + siteUrl);
                            Logger.i("Image domain: " + imgDomain);
                            break;
                        }
                    }
                } catch (JsonValidator.ValidationException e) {
                    Logger.w("Failed to parse response from domain: " + siteUrl, e);
                    // 继续尝试下一个域名
                }
            }

            if (TextUtils.isEmpty(imgDomain)) {
                Logger.w("Failed to initialize Jianpian: no valid domain found");
            }

        } catch (JsonValidator.ValidationException e) {
            Logger.e("Failed to parse DNS resolution response", e);
            throw new Exception("Jianpian initialization failed: invalid DNS response", e);
        }
    }

    /**
     * 获取首页内容
     * <p>
     * 使用 JsonValidator 验证响应，安全提取分类数据。
     * </p>
     *
     * @param filter 是否需要筛选
     * @return 分类列表 JSON
     * @throws Exception 获取失败异常
     */
    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Class> classes = new ArrayList<>();

        try {
            String response = OkHttp.string(siteUrl + "/api/v2/settings/homeCategory");
            if (TextUtils.isEmpty(response)) {
                Logger.w("Home category response is empty");
                return Result.string(classes, new JsonObject());
            }

            // 使用 JsonValidator 验证响应
            JsonObject homeCategory = JsonValidator.validateResponse(response, "object");
            JsonArray dataArray = Json.safeGetJsonArray(homeCategory, "data");

            for (JsonElement element : dataArray) {
                if (!element.isJsonObject()) continue;

                JsonObject item = element.getAsJsonObject();
                String name = Json.safeGetString(item, "name", "");
                String id = Json.safeGetString(item, "id", "");

                if (name.equals("推荐")) continue;
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(id)) continue;

                classes.add(new Class(id, name));
            }

            // 解析扩展配置
            JsonElement filterObj = TextUtils.isEmpty(extend) ?
                    new JsonObject() : JsonParser.parseString(extend);

            return Result.string(classes, filterObj);

        } catch (JsonValidator.ValidationException e) {
            Logger.e("Failed to parse home content response", e);
            return Result.string(classes, new JsonObject());
        }
    }

    @Override
    public String homeVideoContent() {
        List<Vod> list = new ArrayList<>();
        String url = siteUrl + "/api/slide/list?pos_id=88";
        Resp resp = Resp.objectFrom(OkHttp.string(url, getHeader()));
        for (Data data : resp.getData()) list.add(data.homeVod(imgDomain));
        return Result.string(list);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        if (tid.endsWith("/{pg}")) return searchContent(tid.split("/")[0], pg);
        if (tid.equals("50") || tid.equals("99") || tid.equals("111")) {
            List<Vod> list = new ArrayList<>();
            String url = siteUrl + String.format("/api/dyTag/list?category_id=%s&page=%s", tid, pg);
            Resp resp = Resp.objectFrom(OkHttp.string(url, getHeader()));
            for (Data data : resp.getData()) for (Data dataList : data.getDataList()) list.add(dataList.vod(imgDomain));
            return Result.get().page().vod(list).string();
        } else {
            List<Vod> list = new ArrayList<>();
            HashMap<String, String> ext = new HashMap<>();
            if (extend != null && !extend.isEmpty()) ext.putAll(extend);
            String area = ext.get("area") == null ? "0" : ext.get("area");
            String year = ext.get("year") == null ? "0" : ext.get("year");
            String by = ext.get("by") == null ? "updata" : ext.get("by");
            String url = siteUrl + String.format("/api/crumb/list?fcate_pid=%s&area=%s&year=%s&type=0&sort=%s&page=%s&category_id=", tid, area, year, by, pg);
            Resp resp = Resp.objectFrom(OkHttp.string(url, getHeader()));
            for (Data data : resp.getData()) list.add(data.vod(imgDomain));
            return Result.string(list);
        }
    }

    @Override
    public String detailContent(List<String> ids) {
        String url = siteUrl + "/api/video/detailv2?id=" + ids.get(0);
        Data data = Detail.objectFrom(OkHttp.string(url, getHeader())).getData();
        Vod vod = data.vod(imgDomain);
        vod.setVodPlayFrom(data.getVodFrom());
        vod.setVodYear(data.getYear());
        vod.setVodArea(data.getArea());
        vod.setTypeName(data.getTypes());
        vod.setVodActor(data.getActors());
        vod.setVodPlayUrl(data.getVodUrl());
        vod.setVodDirector(data.getDirectors());
        vod.setVodContent(data.getDescription());
        return Result.string(vod);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        return Result.get().url(id).header(getHeader()).string();
    }

    @Override
    public String searchContent(String key, boolean quick) {
        return searchContent(key, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) {
        return searchContent(key, pg);
    }

    public String searchContent(String key, String pg) {
        List<Vod> list = new ArrayList<>();
        String url = siteUrl + String.format("/api/v2/search/videoV2?key=%s&category_id=88&page=%s&pageSize=20", URLEncoder.encode(key), pg);
        Search search = Search.objectFrom(OkHttp.string(url, getHeader()));
        for (Search data : search.getData()) list.add(data.vod(imgDomain));
        return Result.string(list);
    }
}