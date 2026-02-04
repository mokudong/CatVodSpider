package com.github.catvod.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * 视频点播对象（VOD - Video On Demand）
 * <p>
 * 表示一个视频点播资源的完整信息，包括视频ID、名称、封面、
 * 演职员、剧情简介以及播放地址等。
 * </p>
 *
 * <h3>核心字段说明：</h3>
 * <ul>
 *   <li>{@link #vodId} - 视频唯一标识（必需）</li>
 *   <li>{@link #vodName} - 视频名称（必需）</li>
 *   <li>{@link #vodPic} - 封面图片URL（必需）</li>
 *   <li>{@link #vodPlayFrom} - 播放源列表（$$$分隔）</li>
 *   <li>{@link #vodPlayUrl} - 播放地址列表（必须与vodPlayFrom对应）</li>
 * </ul>
 *
 * <h3>播放地址格式说明：</h3>
 * <pre>
 * vodPlayFrom: "播放源1$$$播放源2$$$播放源3"
 * vodPlayUrl: "第1集$url1#第2集$url2$$$01$url3#02$url4$$$集1$url5"
 *
 * 格式规则：
 * - $$$ 分隔不同的播放源
 * - # 分隔同一播放源的不同集数
 * - $ 分隔集数名称和播放地址
 * - 两个字符串中 $$$ 的数量必须相等
 * </pre>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * Vod vod = new Vod();
 * vod.setVodId("12345");
 * vod.setVodName("电影名称");
 * vod.setVodPic("https://example.com/cover.jpg");
 * vod.setVodRemarks("HD/2024");
 * vod.setVodPlayFrom("默认$$$备用");
 * vod.setVodPlayUrl("第1集$http://cdn1.com/1.m3u8#第2集$http://cdn1.com/2.m3u8$$$01$http://cdn2.com/1.m3u8");
 *
 * return Result.string(vod);
 * </pre>
 *
 * @author CatVod
 * @see Result
 */
public class Vod {

    /**
     * 分类名称
     * <p>视频所属的分类，如"电影"、"电视剧"等。</p>
     */
    @SerializedName("type_name")
    private String typeName;

    /**
     * 视频唯一标识
     * <p>必需字段，用于唯一标识一个视频资源。</p>
     * <p>通常来自视频源的ID，用于获取详情和播放地址。</p>
     */
    @SerializedName("vod_id")
    private String vodId;

    /**
     * 视频名称
     * <p>必需字段，显示在UI上的视频标题。</p>
     */
    @SerializedName("vod_name")
    private String vodName;

    /**
     * 封面图片URL
     * <p>必需字段，视频封面的图片地址。</p>
     */
    @SerializedName("vod_pic")
    private String vodPic;

    /**
     * 备注信息
     * <p>显示在视频名称旁边的额外信息，如"更新至第10集"、"HD"、"9.2分"等。</p>
     */
    @SerializedName("vod_remarks")
    private String vodRemarks;

    /**
     * 年份
     * <p>视频的上映年份，如"2024"。</p>
     */
    @SerializedName("vod_year")
    private String vodYear;

    /**
     * 地区
     * <p>视频的制作地区，如"中国大陆"、"美国"、"香港"等。</p>
     */
    @SerializedName("vod_area")
    private String vodArea;

    /**
     * 演员列表
     * <p>视频的主要演员，多个演员用逗号分隔。</p>
     */
    @SerializedName("vod_actor")
    private String vodActor;

    /**
     * 导演
     * <p>视频的导演。</p>
     */
    @SerializedName("vod_director")
    private String vodDirector;

    /**
     * 剧情简介
     * <p>视频的详细描述和剧情介绍。</p>
     */
    @SerializedName("vod_content")
    private String vodContent;

    /**
     * 播放源列表
     * <p>多个播放源用 $$$ 分隔，如"播放源1$$$播放源2"。</p>
     * <p>与 {@link #vodPlayUrl} 配合使用，两个字符串中 $$$ 的数量必须相等。</p>
     */
    @SerializedName("vod_play_from")
    private String vodPlayFrom;

    /**
     * 播放地址列表
     * <p>格式：集数名称$地址#集数名称$地址$$$集数名称$地址</p>
     * <p>必须与 {@link #vodPlayFrom} 一一对应。</p>
     */
    @SerializedName("vod_play_url")
    private String vodPlayUrl;

    /**
     * 视频标签
     * <p>用于标识视频的特殊类型，如"folder"表示文件夹，"file"表示文件。</p>
     */
    @SerializedName("vod_tag")
    private String vodTag;

    /**
     * 操作指令
     * <p>用于触发特定的UI操作。</p>
     */
    @SerializedName("action")
    private String action;

    /**
     * 显示样式
     * <p>控制视频在UI中的显示样式，如矩形、圆形、列表等。</p>
     */
    @SerializedName("style")
    private Style style;

    /**
     * 从JSON字符串创建Vod对象
     *
     * @param str JSON字符串
     * @return Vod对象，解析失败时返回空对象
     */
    public static Vod objectFrom(String str) {
        Vod item = new Gson().fromJson(str, Vod.class);
        return item == null ? new Vod() : item;
    }

    /**
     * 创建带操作指令的Vod对象
     *
     * @param action 操作指令
     * @return Vod对象
     */
    public static Vod action(String action) {
        Vod vod = new Vod();
        vod.action = action;
        return vod;
    }

    /**
     * 默认构造函数
     */
    public Vod() {
    }

    /**
     * 构造函数（基础信息）
     *
     * @param vodId 视频ID
     * @param vodName 视频名称
     * @param vodPic 封面URL
     */
    public Vod(String vodId, String vodName, String vodPic) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
    }

    /**
     * 构造函数（含备注）
     *
     * @param vodId 视频ID
     * @param vodName 视频名称
     * @param vodPic 封面URL
     * @param vodRemarks 备注信息
     */
    public Vod(String vodId, String vodName, String vodPic, String vodRemarks) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
    }

    /**
     * 构造函数（含操作）
     *
     * @param vodId 视频ID
     * @param vodName 视频名称
     * @param vodPic 封面URL
     * @param vodRemarks 备注信息
     * @param action 操作指令
     */
    public Vod(String vodId, String vodName, String vodPic, String vodRemarks, String action) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
        setAction(action);
    }

    /**
     * 构造函数（含样式）
     *
     * @param vodId 视频ID
     * @param vodName 视频名称
     * @param vodPic 封面URL
     * @param vodRemarks 备注信息
     * @param style 显示样式
     */
    public Vod(String vodId, String vodName, String vodPic, String vodRemarks, Style style) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
        setStyle(style);
    }

    /**
     * 构造函数（含样式和操作）
     *
     * @param vodId 视频ID
     * @param vodName 视频名称
     * @param vodPic 封面URL
     * @param vodRemarks 备注信息
     * @param style 显示样式
     * @param action 操作指令
     */
    public Vod(String vodId, String vodName, String vodPic, String vodRemarks, Style style, String action) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
        setStyle(style);
        setAction(action);
    }

    /**
     * 构造函数（文件夹类型）
     *
     * @param vodId 文件夹ID
     * @param vodName 文件夹名称
     * @param vodPic 文件夹图标
     * @param vodRemarks 备注信息
     * @param folder 是否为文件夹
     */
    public Vod(String vodId, String vodName, String vodPic, String vodRemarks, boolean folder) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
        setVodTag(folder ? "folder" : "file");
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public void setVodRemarks(String vodRemarks) {
        this.vodRemarks = vodRemarks;
    }

    public void setVodYear(String vodYear) {
        this.vodYear = vodYear;
    }

    public void setVodArea(String vodArea) {
        this.vodArea = vodArea;
    }

    public void setVodActor(String vodActor) {
        this.vodActor = vodActor;
    }

    public void setVodDirector(String vodDirector) {
        this.vodDirector = vodDirector;
    }

    public void setVodContent(String vodContent) {
        this.vodContent = vodContent;
    }

    public String getVodContent() {
        return vodContent;
    }

    public void setVodPlayFrom(String vodPlayFrom) {
        this.vodPlayFrom = vodPlayFrom;
    }

    public void setVodPlayUrl(String vodPlayUrl) {
        this.vodPlayUrl = vodPlayUrl;
    }

    public String getVodPlayUrl() {
        return vodPlayUrl;
    }

    public void setVodTag(String vodTag) {
        this.vodTag = vodTag;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * 显示样式类
     * <p>
     * 用于控制视频在UI中的显示方式，支持矩形、圆形、全屏、列表等样式。
     * </p>
     *
     * <h3>使用示例：</h3>
     * <pre>
     * // 默认矩形（4:3）
     * vod.setStyle(Style.rect());
     *
     * // 自定义宽高比（16:9）
     * vod.setStyle(Style.rect(0.5625f));
     *
     * // 圆形
     * vod.setStyle(Style.oval());
     *
     * // 全屏模式
     * vod.setStyle(Style.full());
     *
     * // 列表样式
     * vod.setStyle(Style.list());
     * </pre>
     */
    public static class Style {

        /**
         * 样式类型
         * <p>可选值：rect（矩形）、oval（圆形）、full（全屏）、list（列表）</p>
         */
        @SerializedName("type")
        private String type;

        /**
         * 宽高比
         * <p>用于矩形样式，默认0.75（4:3）</p>
         */
        @SerializedName("ratio")
        private Float ratio;

        /**
         * 创建默认矩形样式（4:3）
         *
         * @return Style对象
         */
        public static Style rect() {
            return rect(0.75f);
        }

        /**
         * 创建自定义宽高比的矩形样式
         *
         * @param ratio 宽高比，如0.5625（16:9）、0.75（4:3）
         * @return Style对象
         */
        public static Style rect(float ratio) {
            return new Style("rect", ratio);
        }

        /**
         * 创建圆形样式
         *
         * @return Style对象
         */
        public static Style oval() {
            return new Style("oval", 1.0f);
        }

        /**
         * 创建全屏样式
         *
         * @return Style对象
         */
        public static Style full() {
            return new Style("full");
        }

        /**
         * 创建列表样式
         *
         * @return Style对象
         */
        public static Style list() {
            return new Style("list");
        }

        /**
         * 构造函数（仅类型）
         *
         * @param type 样式类型
         */
        public Style(String type) {
            this.type = type;
        }

        /**
         * 构造函数（类型+宽高比）
         *
         * @param type 样式类型
         * @param ratio 宽高比
         */
        public Style(String type, Float ratio) {
            this.type = type;
            this.ratio = ratio;
        }
    }
}
