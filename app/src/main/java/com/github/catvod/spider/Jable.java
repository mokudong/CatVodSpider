package com.github.catvod.spider;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;
import com.orhanobut.logger.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Jable extends Spider {

    private static final String siteUrl = "https://jable.tv";
    private static final String cateUrl = siteUrl + "/categories/";
    private static final String detailUrl = siteUrl + "/videos/";
    private static final String searchUrl = siteUrl + "/search/";

    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }

    @Override
    public String homeContent(boolean filter) {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(cateUrl, getHeaders()));
        for (Element element : doc.select("div.img-box > a")) {
            String href = element.attr("href");
            String[] parts = href.split("/");

            if (parts.length <= 4) {
                Logger.w("Jable: Invalid category URL format: " + href);
                continue;
            }

            String typeId = parts[4];
            String typeName = element.select("div.absolute-center > h4").text();

            if (typeId.isEmpty() || typeName.isEmpty()) {
                continue;
            }

            classes.add(new Class(typeId, typeName));
        }
        doc = Jsoup.parse(OkHttp.string(siteUrl, getHeaders()));
        for (Element element : doc.select("div.video-img-box")) {
            String pic = element.select("img").attr("data-src");
            String url = element.select("a").attr("href");
            String name = element.select("div.detail > h6").text();

            if (pic.endsWith(".gif") || name.isEmpty()) continue;

            String[] parts = url.split("/");
            if (parts.length <= 4) {
                Logger.w("Jable: Invalid video URL format: " + url);
                continue;
            }

            String id = parts[4];
            list.add(new Vod(id, name, pic));
        }
        return Result.string(classes, list);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        List<Vod> list = new ArrayList<>();
        String target = cateUrl + tid + "/?mode=async&function=get_block&block_id=list_videos_common_videos_list&sort_by=post_date&from=" + String.format(Locale.getDefault(), "%02d", Integer.parseInt(pg)) + "&_=" + System.currentTimeMillis();
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("div.video-img-box")) {
            String pic = element.select("img").attr("data-src");
            String url = element.select("a").attr("href");
            String name = element.select("div.detail > h6").text();

            String[] parts = url.split("/");
            if (parts.length <= 4) {
                Logger.w("Jable: Invalid video URL format in category: " + url);
                continue;
            }

            String id = parts[4];
            list.add(new Vod(id, name, pic));
        }
        return Result.string(list);
    }

    @Override
    public String detailContent(List<String> ids) {
        Document doc = Jsoup.parse(OkHttp.string(detailUrl.concat(ids.get(0)).concat("/"), getHeaders()));
        String name = doc.select("meta[property=og:title]").attr("content");
        String pic = doc.select("meta[property=og:image]").attr("content");

        // 安全获取年份，避免 ArrayIndexOutOfBoundsException
        String year = "";
        org.jsoup.select.Elements yearElements = doc.select("span.inactive-color");
        if (!yearElements.isEmpty()) {
            year = yearElements.get(0).text().replace("上市於 ", "");
        }

        // 安全获取播放 URL
        String hlsUrl = Util.getVar(doc.html(), "hlsUrl");
        if (hlsUrl == null || hlsUrl.isEmpty()) {
            Logger.w("Jable: Failed to extract hlsUrl for video: " + ids.get(0));
            hlsUrl = "";
        }

        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(pic);
        vod.setVodYear(year);
        vod.setVodName(name);
        vod.setVodPlayFrom("Jable");
        vod.setVodPlayUrl("播放$" + hlsUrl);
        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) {
        List<Vod> list = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(searchUrl.concat(URLEncoder.encode(key)).concat("/"), getHeaders()));
        for (Element element : doc.select("div.video-img-box")) {
            String pic = element.select("img").attr("data-src");
            String url = element.select("a").attr("href");
            String name = element.select("div.detail > h6").text();

            String[] parts = url.split("/");
            if (parts.length <= 4) {
                Logger.w("Jable: Invalid video URL format in search: " + url);
                continue;
            }

            String id = parts[4];
            list.add(new Vod(id, name, pic));
        }
        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        return Result.get().url(id).header(getHeaders()).string();
    }
}
