# ============================================================
# CatVodSpider ProGuard 配置（加固版）
# 说明：仅保留必要的类和方法，增强混淆强度
# ============================================================

# 启用优化（默认关闭，启用可减小 APK 体积）
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# 包名扁平化（增强混淆）
-flattenpackagehierarchy com.github.catvod.spider.merge
-repackageclasses com.github.catvod.obfuscated

# ============================================================
# 忽略警告（第三方库）
# ============================================================
-dontwarn org.slf4j.**
-dontwarn org.xmlpull.v1.**
-dontwarn com.google.re2j.**
-dontwarn android.content.res.**
-dontwarn okhttp3.**
-dontwarn org.ietf.jgss.**
-dontwarn javax.**

# ============================================================
# 基础框架（最小化保留）
# ============================================================

# slf4j - 仅保留接口
-keep interface org.slf4j.** { *; }

# AndroidX - 仅保留必要类（不保留所有）
-keep class androidx.core.app.ComponentActivity { *; }
-keep class androidx.core.content.FileProvider { *; }

# ============================================================
# 爬虫框架（核心）
# ============================================================

# Spider 基类 - 必须保留（反射加载）
-keep public class com.github.catvod.crawler.Spider {
    public <init>();
    public <init>(android.content.Context, java.lang.String);
    public void init(android.content.Context, java.lang.String);
    public java.lang.String homeContent(boolean);
    public java.lang.String categoryContent(java.lang.String, java.lang.String, boolean, java.util.HashMap);
    public java.lang.String detailContent(java.util.List);
    public java.lang.String searchContent(java.lang.String, boolean);
    public java.lang.String playerContent(java.lang.String, java.lang.String, java.util.List);
    public void destroy();
}

# Spider 实现类 - 仅保留构造函数和继承的方法（不暴露所有 public 方法）
-keep public class com.github.catvod.spider.** extends com.github.catvod.crawler.Spider {
    public <init>();
    public <init>(android.content.Context, java.lang.String);
}

# SpiderDebug - 保留日志方法
-keep class com.github.catvod.crawler.SpiderDebug {
    public static void log(java.lang.String);
    public static void log(java.lang.Throwable);
    public static void logSanitized(java.lang.String);
}

# JavaScript 桥接类
-keep class com.github.catvod.js.Function { *; }

# ============================================================
# 网络库（OkHttp）
# ============================================================
-keep class okio.** { *; }
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }

# ============================================================
# JavaScript 引擎（QuickJS）
# ============================================================
-keep class com.whl.quickjs.** { *; }

# ============================================================
# 文件协议库
# ============================================================

# Sardine (WebDAV)
-keep class com.thegrizzlylabs.sardineandroid.** { *; }

# SMBJ (SMB 文件共享)
-keep class com.hierynomus.** { *; }
-keep class net.engio.mbassy.** { *; }

# ============================================================
# 工具库
# ============================================================

# Logger
-keep class com.orhanobut.logger.** { *; }

# Gson (JSON 序列化) - 防止字段被混淆
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留 Bean 类（防止 Gson 序列化失败）
-keep class com.github.catvod.bean.** { *; }

# ============================================================
# 加密库（AndroidX Security）
# ============================================================
-keep class androidx.security.crypto.** { *; }

# ============================================================
# 通用规则
# ============================================================

# 保留注解
-keepattributes *Annotation*

# 保留行号信息（便于调试崩溃日志）
-keepattributes SourceFile,LineNumberTable

# 保留泛型信息
-keepattributes Signature

# 保留 Native 方法（JNI）
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 Serializable 类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}