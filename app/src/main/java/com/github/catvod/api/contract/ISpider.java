package com.github.catvod.api.contract;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬虫接口契约
 * <p>
 * 定义所有爬虫必须实现的标准方法，提供统一的抽象层。
 * 这个接口使得爬虫实现可以与具体框架解耦，便于测试和扩展。
 * </p>
 * <p>
 * <b>设计原则：</b>
 * <ul>
 *   <li>接口隔离：只定义必要的方法</li>
 *   <li>依赖倒置：上层模块依赖接口而非具体实现</li>
 *   <li>开闭原则：对扩展开放，对修改关闭</li>
 * </ul>
 * </p>
 *
 * @author CatVod Team
 * @since 2.0.0
 */
public interface ISpider {

    /**
     * 初始化爬虫
     * <p>
     * 在第一次使用爬虫前调用，用于初始化配置和资源。
     * </p>
     *
     * @param context Android Context
     * @param extend  扩展参数（来自配置文件）
     */
    void init(Context context, String extend);

    /**
     * 获取首页内容（分类列表）
     *
     * @param filter 是否需要筛选条件
     * @return JSON 字符串
     */
    String homeContent(boolean filter);

    /**
     * 获取分类内容（视频列表）
     *
     * @param tid    分类ID
     * @param pg     页码
     * @param filter 是否启用筛选
     * @param extend 筛选参数
     * @return JSON 字符串
     */
    String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend);

    /**
     * 获取视频详情
     *
     * @param ids 视频ID列表
     * @return JSON 字符串
     */
    String detailContent(List<String> ids);

    /**
     * 搜索视频
     *
     * @param keyword 搜索关键词
     * @param quick   是否快速搜索
     * @return JSON 字符串
     */
    String searchContent(String keyword, boolean quick);

    /**
     * 获取播放地址
     *
     * @param flag     线路标识
     * @param id       播放ID
     * @param vipFlags VIP线路标识列表
     * @return JSON 字符串
     */
    String playerContent(String flag, String id, List<String> vipFlags);

    /**
     * 销毁爬虫（释放资源）
     */
    void destroy();

    /**
     * 代理请求（可选实现）
     * <p>
     * 用于处理需要代理的特殊请求。
     * </p>
     *
     * @param params 请求参数
     * @return 响应数组：[状态码, Content-Type, 数据]
     */
    default Object[] proxy(Map<String, String> params) {
        return null;
    }

    /**
     * 获取手动搜索视频（可选实现）
     * <p>
     * 与 searchContent 的区别：manualVideoCheck 用于手动验证搜索结果。
     * </p>
     *
     * @return JSON 字符串
     */
    default String manualVideoCheck() {
        return "";
    }

    /**
     * 获取首页视频内容（可选实现）
     * <p>
     * 用于在首页直接展示推荐视频。
     * </p>
     *
     * @return JSON 字符串
     */
    default String homeVideoContent() {
        return "";
    }
}
