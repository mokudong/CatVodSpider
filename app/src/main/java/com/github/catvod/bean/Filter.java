package com.github.catvod.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 筛选条件对象
 * <p>
 * 表示视频分类的筛选条件，如年份、地区、排序等。
 * 用户通过筛选条件可以精确查找想要的内容。
 * </p>
 *
 * <h3>结构说明：</h3>
 * <pre>
 * Filter
 * ├── key: 筛选条件的键（提交给后端的参数名）
 * ├── name: 筛选条件的显示名称
 * ├── init: 初始值（可选）
 * └── value: 筛选选项列表
 *     └── Value
 *         ├── n: 显示名称
 *         └── v: 实际值
 * </pre>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 创建年份筛选
 * List&lt;Filter.Value&gt; years = new ArrayList&lt;&gt;();
 * years.add(new Filter.Value("全部", ""));
 * years.add(new Filter.Value("2024", "2024"));
 * years.add(new Filter.Value("2023", "2023"));
 * Filter yearFilter = new Filter("year", "年份", years);
 *
 * // 创建排序筛选
 * List&lt;Filter.Value&gt; sorts = new ArrayList&lt;&gt;();
 * sorts.add(new Filter.Value("最新", "time"));
 * sorts.add(new Filter.Value("最热", "hot"));
 * Filter sortFilter = new Filter("sort", "排序", sorts);
 *
 * // 为分类添加筛选
 * LinkedHashMap&lt;String, List&lt;Filter&gt;&gt; filters = new LinkedHashMap&lt;&gt;();
 * filters.put("1", Arrays.asList(yearFilter, sortFilter));
 *
 * // 返回给TVBox
 * return Result.string(classes, filters);
 * </pre>
 *
 * <h3>用户选择后的参数：</h3>
 * <p>
 * 当用户选择"2024"和"最新"时，在 {@link com.github.catvod.crawler.Spider#categoryContent}
 * 方法的 extend 参数中会收到：
 * <pre>
 * {
 *   "year": "2024",
 *   "sort": "time"
 * }
 * </pre>
 *
 * @author CatVod
 * @see Result
 */
public class Filter {

    /**
     * 筛选条件的键
     * <p>
     * 提交给API的参数名，用户选择后作为key返回给爬虫。
     * </p>
     */
    @SerializedName("key")
    private String key;

    /**
     * 筛选条件的显示名称
     * <p>
     * 显示在UI上的筛选条件名称，如"年份"、"地区"、"排序"等。
     * </p>
     */
    @SerializedName("name")
    private String name;

    /**
     * 初始值
     * <p>
     * 筛选条件的默认选中值，可选。
     * </p>
     */
    @SerializedName("init")
    private String init;

    /**
     * 筛选选项列表
     * <p>
     * 该筛选条件的所有可选值列表。
     * </p>
     */
    @SerializedName("value")
    private List<Value> value;

    /**
     * 构造函数
     *
     * @param key 筛选条件的键
     * @param name 筛选条件的显示名称
     * @param value 筛选选项列表
     */
    public Filter(String key, String name, List<Value> value) {
        this.key = key;
        this.name = name;
        this.value = value;
    }

    /**
     * 筛选选项值对象
     * <p>
     * 表示筛选条件中的一个可选值。
     * </p>
     *
     * <h3>字段说明：</h3>
     * <ul>
     *   <li>{@link #n} - 显示名称，在UI上展示给用户</li>
     *   <li>{@link #v} - 实际值，提交给API的参数值</li>
     * </ul>
     *
     * <h3>示例：</h3>
     * <pre>
     * // 显示名称和实际值相同
     * new Filter.Value("2024", "2024")
     *
     * // 显示名称和实际值不同
     * new Filter.Value("全部", "")           // 显示"全部"，实际值为空
     * new Filter.Value("最新", "time")       // 显示"最新"，实际值为"time"
     * </pre>
     */
    public static class Value {

        /**
         * 显示名称
         * <p>
         * 在UI上展示给用户看的文本。
         * </p>
         */
        @SerializedName("n")
        private String n;

        /**
         * 实际值
         * <p>
         * 用户选择后提交给API的实际参数值。
         * </p>
         */
        @SerializedName("v")
        private String v;

        /**
         * 构造函数（显示名称和实际值相同）
         * <p>
         * 当显示名称和实际值相同时使用。
         * </p>
         *
         * @param value 显示名称和实际值
         */
        public Value(String value) {
            this.n = value;
            this.v = value;
        }

        /**
         * 构造函数（指定显示名称和实际值）
         *
         * @param n 显示名称
         * @param v 实际值
         */
        public Value(String n, String v) {
            this.n = n;
            this.v = v;
        }
    }
}
