package com.github.catvod.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 视频分类对象
 * <p>
 * 表示一个视频分类，如"电影"、"电视剧"、"综艺"等。
 * 用于在首页显示分类列表，以及获取分类下的视频内容。
 * </p>
 *
 * <h3>核心字段：</h3>
 * <ul>
 *   <li>{@link #typeId} - 分类ID，用于获取分类内容（必需）</li>
 *   <li>{@link #typeName} - 分类名称，显示在UI上（必需）</li>
 *   <li>{@link #typeFlag} - 分类标志，可选的特殊标记</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 创建分类
 * Class movie = new Class("1", "电影");
 * Class tvShow = new Class("2", "电视剧");
 * Class anime = new Class("3", "动漫");
 *
 * // 构建分类列表
 * List&lt;Class&gt; classes = new ArrayList&lt;&gt;();
 * classes.add(movie);
 * classes.add(tvShow);
 * classes.add(anime);
 *
 * // 返回给TVBox
 * return Result.get().classes(classes).string();
 * </pre>
 *
 * @author CatVod
 * @see Result
 */
public class Class {

    /**
     * 分类ID
     * <p>
     * 必需字段，用于唯一标识一个分类。
     * 在 {@link com.github.catvod.crawler.Spider#categoryContent} 方法中，
     * 此ID作为tid参数传入，用于获取该分类下的视频列表。
     * </p>
     */
    @SerializedName("type_id")
    private String typeId;

    /**
     * 分类名称
     * <p>
     * 必需字段，显示在UI上的分类名称，如"电影"、"电视剧"等。
     * </p>
     */
    @SerializedName("type_name")
    private String typeName;

    /**
     * 分类标志
     * <p>
     * 可选字段，用于特殊的分类标记或扩展信息。
     * </p>
     */
    @SerializedName("type_flag")
    private String typeFlag;

    /**
     * 从JSON字符串解析分类列表
     *
     * @param str JSON格式的分类列表字符串
     * @return 分类列表
     *
     * <h4>示例：</h4>
     * <pre>
     * String json = "[{\"type_id\":\"1\",\"type_name\":\"电影\"}]";
     * List&lt;Class&gt; classes = Class.arrayFrom(json);
     * </pre>
     */
    public static List<Class> arrayFrom(String str) {
        Type listType = new TypeToken<List<Class>>() {}.getType();
        return new Gson().fromJson(str, listType);
    }

    /**
     * 构造函数（分类ID即名称）
     * <p>
     * 当分类ID和名称相同时使用此构造函数。
     * </p>
     *
     * @param typeId 分类ID，同时作为分类名称
     */
    public Class(String typeId) {
        this(typeId, typeId);
    }

    /**
     * 构造函数（指定ID和名称）
     *
     * @param typeId 分类ID
     * @param typeName 分类名称
     */
    public Class(String typeId, String typeName) {
        this(typeId, typeName, null);
    }

    /**
     * 构造函数（完整参数）
     *
     * @param typeId 分类ID
     * @param typeName 分类名称
     * @param typeFlag 分类标志
     */
    public Class(String typeId, String typeName, String typeFlag) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.typeFlag = typeFlag;
    }

    /**
     * 获取分类ID
     *
     * @return 分类ID
     */
    public String getTypeId() {
        return typeId;
    }

    /**
     * 判断两个分类是否相等
     * <p>
     * 通过比较分类ID来判断是否为同一个分类。
     * </p>
     *
     * @param obj 要比较的对象
     * @return true=分类相同，false=分类不同
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Class)) return false;
        Class it = (Class) obj;
        return getTypeId().equals(it.getTypeId());
    }
}
