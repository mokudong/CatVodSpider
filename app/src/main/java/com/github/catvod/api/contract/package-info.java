/**
 * API 契约包
 * <p>
 * 包含所有的接口定义，用于实现依赖倒置原则（DIP）和接口隔离原则（ISP）。
 * </p>
 * <p>
 * <b>设计模式：</b>
 * <ul>
 *   <li>依赖倒置（DIP）：高层模块依赖抽象接口，而非具体实现</li>
 *   <li>接口隔离（ISP）：客户端不应被迫依赖它不使用的接口</li>
 *   <li>开闭原则（OCP）：对扩展开放，对修改关闭</li>
 * </ul>
 * </p>
 * <p>
 * <b>架构优势：</b>
 * <ul>
 *   <li>降低耦合：模块间通过接口交互，减少直接依赖</li>
 *   <li>易于测试：可以轻松注入 Mock 实现进行单元测试</li>
 *   <li>灵活扩展：新增实现无需修改现有代码</li>
 *   <li>替换实现：可以无缝切换不同的实现（如 HTTP 库）</li>
 * </ul>
 * </p>
 * <p>
 * <b>使用示例：</b>
 * <pre>
 * // 爬虫依赖接口而非具体实现
 * public class MySpider implements ISpider {
 *     private final IHttpClient httpClient;
 *     private final IStorage storage;
 *
 *     // 依赖注入
 *     public MySpider(IHttpClient httpClient, IStorage storage) {
 *         this.httpClient = httpClient;
 *         this.storage = storage;
 *     }
 *
 *     public String searchContent(String keyword, boolean quick) {
 *         // 使用接口方法，不关心具体实现
 *         String response = httpClient.get("https://api.example.com/search?q=" + keyword);
 *         storage.putString("last_search", keyword);
 *         return response;
 *     }
 * }
 *
 * // 单元测试时注入 Mock
 * IHttpClient mockHttpClient = mock(IHttpClient.class);
 * when(mockHttpClient.get(anyString())).thenReturn("{\"result\":[]}");
 *
 * MySpider spider = new MySpider(mockHttpClient, mockStorage);
 * String result = spider.searchContent("test", false);
 * </pre>
 * </p>
 *
 * @author CatVod Team
 * @version 2.0.0
 * @since 2.0.0
 */
package com.github.catvod.api.contract;
